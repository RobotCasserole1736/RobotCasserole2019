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
    # ###################################################################################################
    ## Process function with USB output
    def processNoUSB(self, inframe):
    
        #Initialization process
        inimg,pipeline_start_time=self.InitFunction(inframe)

        #Creates mask of image
        inimg,mask,maskoutput=self.LightFilter(inimg)
        
        #finds contours
        (contours) = self.findcontours(mask)

        #filters contours based on width and area
        (filteredContours) = self.filtercontours(contours, 40.0, 10.0)
        
        #
        targetedContours,anglestuff = self.TargetDetection(maskoutput,inimg,filteredContours)
        pipeline_end_time = datetime.now() - pipline_start_time
        self.pipelineDelay_us = pipeline_end_time.microseconds
        #sends output over serial
        jevois.sendSerial("["+str(targetedContours)+","+str(self.pipelineDelay_us)+"]")
        
        
    
    def process(self, inframe, outframe):
        
        #Init stuff,starts timer
        self.InitFunction(inframe)
        
        #Calibrates Camera
        self.loadCameraCalibration()
        
        #Filters image based on predefined filters
        self.LightFilter()
        
        #Finds contours
        contours=self.findcontours(self.mask)
        
        #Filters contours based on width and area
        filteredContours = self.filtercontours(contours, 100.0, 10.0)
        
        #Detects target, will eventually return translation and rotation vector
        pairlist,ret,rvec,tvec = self.TargetDetection(self.maskoutput,self.inimg,filteredContours)
        
        #Timer stuff
        pipelineDelay_us = (datetime.now() - self.pipeline_start_time).microseconds
        
        #sends output over serial
        jevois.sendSerial("["+str(ret)+","+str(rvec)+","+str(tvec)+","+str(pairlist)+","+str(pipelineDelay_us)+"]")
        
        #sends image
        #outframe.sendCvRGB(np.concatenate((self.inimg, self.maskoutput), axis=1))
        outframe.sendCvRGB(self.inimg)
        
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
        self.maskoutput = cv2.cvtColor(self.mask, cv2.COLOR_GRAY2BGR)
            
    def loadCameraCalibration(self):
        cpf = "/jevois/share/camera/calibration{}x{}.yaml".format(self.w, self.h)
        fs = cv2.FileStorage(cpf, cv2.FILE_STORAGE_READ)
        if (fs.isOpened()):
            self.camMatrix = fs.getNode("camera_matrix").mat()
            self.distCoeffs = fs.getNode("distortion_coefficients").mat()
            jevois.LINFO("Loaded camera calibration from {}".format(cpf))
        else:
            jevois.LFATAL("Failed to read camera parameters from file [{}]".format(cpf))
    
    
    def TargetDetection(self,maskoutput,inimg,contourinput):
        #initvalues
        pair=0
        rect=0
        anglestuff=[]
        potentialltarget=[]
        potentialrtarget=[]
        contourid=0
        pairlist=[]
        ret=[]
        rvec=[]
        tvec=[]
        ImgPoints=np.array([])
        PairingHighScore=999999.0
        ObjPoints=np.array([(1.9363,-0.5008,0),
                            (0,0,0),
                            (1.3371,5.3248,0),
                            (3.3134,4.8241,0),
                            (12.6905,-0.5008,0),
                            (11.3134,4.8241,0),
                            (13.2497,5.3248,0),
                            (-14.6268,0,0)
                            ],dtype=np.float64)
        CameraMatrix=np.array([[1004.5987693,0,640],[0,1004.5987693,512],[0,0,1]])
        dist_coeffs = np.zeros((4,1))
        #find angles of contours
        for contour in contourinput:
            rect=cv2.minAreaRect(contour)
            angle=rect[2]
            box = cv2.boxPoints(rect)
            box = np.int0(box)
            if (angle<=-10.0 and angle>=-35.0):
                potentialrtarget.append(contourid)
                cv2.drawContours(maskoutput,[box],0,(0,0,255),2)
            elif (angle<=-50.0 and angle>=-75.0):
                potentialltarget.append(contourid)
                cv2.drawContours(maskoutput,[box],0,(255,0,0),2)
            else:
                cv2.drawContours(maskoutput,[box],0,(0,255,0),2)
            contourid+=1
        #PAIRING ALGORITHM
        for ltarget in potentialltarget:
            for rtarget in potentialrtarget:
                #Init values
                PairingScore=0.0
                lrect=cv2.minAreaRect(contourinput[ltarget])
                rrect=cv2.minAreaRect(contourinput[rtarget])
                lbrect=cv2.boundingRect(contourinput[ltarget])
                rbrect=cv2.boundingRect(contourinput[rtarget])
                lcent=lrect[0]
                rcent=rrect[0]
                larea=cv2.contourArea(contourinput[ltarget])
                rarea=cv2.contourArea(contourinput[rtarget])
                if (lcent[0]<=rcent[0]):
                    #Parameters for scoring
                    CenterHeightDistance=abs(lcent[1]-rcent[1])
                    CenterPoint=abs((lcent[0]+rcent[0])-1280)
                    Size=larea+rarea
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
            lcoord=(int(lcoordflt[0]),int(lcoordflt[1]))
            rcoord=(int(rcoordflt[0]),int(rcoordflt[1]))
            cv2.line(maskoutput,lcoord,rcoord,(255,0,0),2)
            self.prevlcent=lcoordflt
            self.prevrcent=rcoordflt
            leftmost=[]
            rightmost=[]
            topmost=[]
            bottommost=[]
            for contour in pair:
                cnt=contourinput[contour]
                leftmost.append(tuple(cnt[cnt[:,:,0].argmin()][0]))
                rightmost.append(tuple(cnt[cnt[:,:,0].argmax()][0]))
                topmost.append(tuple(cnt[cnt[:,:,1].argmin()][0]))
                bottommost.append(tuple(cnt[cnt[:,:,1].argmax()][0]))
            ImgPoints=np.array([bottommost[0],leftmost[0],topmost[0],rightmost[0],bottommost[1],leftmost[1],topmost[1],rightmost[1]], dtype="double")
            ret, rvec, tvec = cv2.solvePnP(ObjPoints,ImgPoints,CameraMatrix,dist_coeffs)
        return pair,ret,rvec,tvec



            
