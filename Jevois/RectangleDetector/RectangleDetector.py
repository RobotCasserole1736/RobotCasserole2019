import libjevois as jevois
import cv2
import numpy as np
import math
import time
from datetime import datetime


## Module used to detect Power Cube (Rectangular Prism) using Jevois
#
# By default, we get the next video frame from the camera as an OpenCV BGR (color) image named 'inimg'.
# We then apply some image processing to it to create an output BGR image named 'outimg'.
# We finally add some text drawings to outimg and send it to host over USB.
#
# 
# @videomapping YUYV 352 288 30.0 YUYV 352 288 30.0 JeVois RectangleDetector
# @email itti\@usc.edu
# @address University of Southern California, HNB-07A, 3641 Watt Way, Los Angeles, CA 90089-2520, USA
# @copyright Copyright (C) 2017 by Laurent Itti, iLab and the University of Southern California
# @mainurl http://jevois.org
# @supporturl http://jevois.org/doc
# @otherurl http://iLab.usc.edu
# @license GPL v3
# @distribution Unrestricted
# @restrictions None
# @ingroup modules
class RectangleDetector:
    # ###################################################################################################
    ## Constructor
    def __init__(self):
        # Instantiate a JeVois Timer to measure our processing framerate:
        self.timer = jevois.Timer("sandbox", 100, jevois.LOG_INFO)
        self.prevlcent=0
        self.prevrcent=0
        self.resetcounter=0
        self.ObjPoints=np.array([(1.9363,-0.5008,0),
                            (0,0,0),
                            (1.3371,5.3248,0),
                            (3.3134,4.8241,0),
                            (12.6905,-0.5008,0),
                            (11.3134,4.8241,0),
                            (13.2497,5.3248,0),
                            (14.6268,0,0)
                            ],dtype=np.float64)
    # ###################################################################################################
    ## Process function with USB output
    def processNoUSB(self, inframe):
    
        #Keeps camera from doing unnecesary drawings
        self.streamCheck=False
        
        #Init stuff,starts timer
        self.InitFunction(inframe)
        
        #Calibrates Camera
        self.loadCameraCalibration()
        
        #Filters image based on predefined filters
        self.LightFilter()
        
        #Finds contours
        contours=self.findcontours(self.mask)
        
        #Filters contours based on width and area
        filteredContours = self.filtercontours(contours, 100.0, 30.0)
        
        #Detects target, will eventually return translation and rotation vector
        self.TargetDetection(filteredContours)
        
        #Timer stuff
        pipelineDelay_us = (datetime.now() - self.pipeline_start_time).microseconds
        
        #sends output over serial
        jevois.sendSerial("{{{},{},{},{},{},{},{},{},{},{}}}\n".format(self.ret,self.yaw,self.xval,self.yval,4,5,self.resetcounter,7,8,pipelineDelay_us))
        #jevois.sendSerial(f"[{ret},{rvec},{tvec},{pipelineDelay_us},{self.resetcounter}]")
        
        
        self.timer.stop()        
        
    
    def process(self, inframe, outframe):
        
        #Camera will draw rectangles on image
        self.streamCheck=True
        
        #Init stuff,starts timer
        self.InitFunction(inframe)
        
        #Calibrates Camera
        self.loadCameraCalibration()
        
        #Filters image based on predefined filters
        self.LightFilter()
        
        #Finds contours
        contours=self.findcontours(self.mask)
        
        #Filters contours based on width and area
        filteredContours = self.filtercontours(contours, 100.0, 30.0)
        
        #Detects target, will eventually return translation and rotation vector
        self.TargetDetection(filteredContours)
        
        #Timer stuff
        pipelineDelay_us = (datetime.now() - self.pipeline_start_time).microseconds
        
        
        
        #sends output over serial
        jevois.sendSerial("{{{},{},{},{},{},{},{},{},{},{}}}\n".format(self.ret,self.yaw,self.xval,self.yval,4,5,self.resetcounter,7,8,pipelineDelay_us))

        #jevois.sendSerial(f"[{ret},{rvec},{tvec},{pipelineDelay_us},{self.resetcounter}]")
        
        #sends image
        #outframe.sendCvRGB(np.concatenate((self.inimg, self.maskoutput), axis=1))
        outframe.sendCvRGB(self.maskoutput)
        
        self.timer.stop()
        
        
        
    #Methods
        
    @staticmethod
    def findcontours(input):
        contours, hierarchy =cv2.findContours(input, mode=cv2.RETR_LIST, method=cv2.CHAIN_APPROX_SIMPLE)
        return contours


    @staticmethod
    def filtercontours(input_contours, min_area, min_width,):
        output = []
        for contour in input_contours:
            x,y,w,h = cv2.boundingRect(contour)
            if (w < min_width or w>600):
                continue
            area = cv2.contourArea(contour)
            if (area < min_area):
                continue
            output.append(contour)
        return output
    

    def InitFunction(self,inframe):
        #Gets the frame from the jevois camera in RGB format
        self.inimg = inframe.getCvRGB()
        
        #Starts timer
        self.pipeline_start_time = datetime.now()
        
        self.h, self.w, chans = self.inimg.shape
        
                

    def LightFilter(self):
        #Sets image to hsv colors
        hsv = cv2.cvtColor(self.inimg, cv2.COLOR_RGB2HSV)
        
        #Defines hsv values of targets
        lowerBrightness = np.array([50,0,75])
        upperBrightness = np.array([100,200,255])
        
        #Creates mask based on hsv range
        self.mask = cv2.inRange(hsv, lowerBrightness, upperBrightness)
        
        #Makes an image using the mask
        if self.streamCheck:
            self.maskoutput = cv2.cvtColor(self.mask, cv2.COLOR_GRAY2BGR)
            
    def loadCameraCalibration(self):
        cpf = "/jevois/share/camera/calibration{}x{}.yaml".format(self.w, self.h)
        fs = cv2.FileStorage(cpf, cv2.FILE_STORAGE_READ)
        if (fs.isOpened()):
            self.camMatrix = fs.getNode("camera_matrix").mat()
            self.distCoeffs = fs.getNode("distortion_coefficients").mat()
        else:
            jevois.LFATAL("Failed to read camera parameters from file [{}]".format(cpf))
    
    
    def TargetDetection(self,contourinput):
        #initvalues
        pair=0
        potentialltarget=[]
        potentiallrect=[]
        potentialrtarget=[]
        potentialrrect=[]
        self.ret="F"
        self.yaw=0
        self.xval=0
        self.yval=0
        PairingHighScore=999999.0
        #find angles of contours
        for i, contour in enumerate(contourinput):
            rect=cv2.minAreaRect(contour)
            angle=rect[2]
            box = cv2.boxPoints(rect)
            box = np.int0(box)
            if (angle<=-10.0 and angle>=-35.0):
                potentialrtarget.append(i)
                potentialrrect.append(rect[0])
                if self.streamCheck:
                    cv2.drawContours(self.maskoutput,[box],0,(0,0,255),2)
            elif (angle<=-50.0 and angle>=-75.0):
                potentialltarget.append(i)
                potentiallrect.append(rect[0])
                if self.streamCheck:
                    cv2.drawContours(self.maskoutput,[box],0,(255,0,0),2)
            elif self.streamCheck:
                cv2.drawContours(self.maskoutput,[box],0,(0,255,0),2)
        #PAIRING ALGORITHM
        for i, ltarget in enumerate(potentialltarget):
            for j, rtarget in enumerate(potentialrtarget):
                lcent=potentiallrect[i]
                rcent=potentialrrect[j]
                if (lcent[0]<=rcent[0]):
                    #Init values
                    PairingScore=0
                    
                    
                    #Parameters for scoring
                    CenterHeightDistance=abs(lcent[1]-rcent[1])
                    CenterPoint=abs((lcent[0]+rcent[0])-1280)
                    TargetDistance=abs(rcent[0]-lcent[0])
                    if((self.prevlcent)!=0):
                        xchange=abs(lcent[0]-self.prevlcent[0])+abs(rcent[0]-self.prevrcent[0])
                        ychange=abs(lcent[1]-self.prevlcent[1])+abs(rcent[1]-self.prevrcent[1])
                        PairingScore=CenterPoint+TargetDistance+xchange+ychange
                    else:
                        PairingScore=CenterPoint+TargetDistance
                    if (PairingScore<=PairingHighScore):
                        PairingHighScore=PairingScore
                        pair=[ltarget,rtarget]
                    
        #Draws line between the two contours
        if (pair!=0): 
            lcoordflt=cv2.minAreaRect(contourinput[pair[0]])[0]
            rcoordflt=cv2.minAreaRect(contourinput[pair[1]])[0]
            if self.streamCheck:
                lcoord=(int(lcoordflt[0]),int(lcoordflt[1]))
                rcoord=(int(rcoordflt[0]),int(rcoordflt[1]))
                cv2.line(self.maskoutput,lcoord,rcoord,(255,0,0),2)
            self.prevlcent=lcoordflt
            self.prevrcent=rcoordflt
            coords2d=[]
            for contour in pair:
                cnt=contourinput[contour]
                coords2d.append(tuple(cnt[cnt[:,:,1].argmax()][0]))
                coords2d.append(tuple(cnt[cnt[:,:,0].argmin()][0]))
                coords2d.append(tuple(cnt[cnt[:,:,1].argmin()][0]))
                coords2d.append(tuple(cnt[cnt[:,:,0].argmax()][0]))
            ImgPoints=np.array([coords2d[0],coords2d[1],coords2d[2],coords2d[3],coords2d[4],coords2d[5],coords2d[6],coords2d[7]], dtype="double")
            self.ret, rvec, tvec = cv2.solvePnP(self.ObjPoints,ImgPoints,self.camMatrix,self.distCoeffs)
            self.ret="T"
            self.yaw=(str(rvec[2]).strip('[]'))
            self.xval=(str(tvec[2]).strip('[]'))
            self.yval=(str(tvec[0]).strip('[]'))
        
    def parseSerial(self, str):
        jevois.LINFO("parseserial received command [{}]".format(str))
        if str == "latch":
            self.prevlcent=0
            self.prevrcent=0
            self.resetcounter+=1
            return "Reset Completed"
        return "ERR Unsupported command"