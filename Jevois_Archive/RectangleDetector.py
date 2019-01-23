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
    
        inimg,pipeline_start_time=self.InitFunction(inframe)

        #
        inimg,mask,maskoutput=self.LightFilter(inimg)
       
        
        #finds contours
        (contours) = self.findcontours(mask)

        #filters contours based on width and area
        (filteredContours) = self.filtercontours(contours, 100.0, 10.0)

        #
        targetedContours,pairlist = self.TargetDetection(maskoutput,inimg,filteredContours)
        pipeline_end_time = datetime.now() - pipeline_start_time
        self.pipelineDelay_us = pipeline_end_time.microseconds
        #sends output over serial
        jevois.sendSerial("["+str(targetedContours)+","+str(pairlist)+","+str(self.pipelineDelay_us)+"]")
        
        #sends image
        #outframe.sendCvRGB(np.concatenate((inimg, maskoutput), axis=1))
        outframe.sendCvRGB(maskoutput)
        
        
        
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
    
    @staticmethod
    def InitFunction(inframe):
        # Gets the frame from the jevois camera in RGB format
        inimg = inframe.getCvRGB()
        
        
        pipeline_start_time = datetime.now()
        
        return inimg,pipeline_start_time
    
    @staticmethod
    def LightFilter(inimg):
        hsv = cv2.cvtColor(inimg, cv2.COLOR_RGB2HSV)
        
        #defines brightness values of targets
        lower_brightness = np.array([50,0,75])
        upper_brightness = np.array([100,200,255])
        
        #creates the mask using the ranges defined in the previous step
        mask = cv2.inRange(hsv, lower_brightness, upper_brightness)
        
        #makes a greyscale image of the mask and sends it out for viewing purposes
        maskoutput = cv2.cvtColor(mask, cv2.COLOR_GRAY2BGR)
    
    
        return inimg,mask,maskoutput
    
    
    
    
    @staticmethod
    def TargetDetection(maskoutput,inimg,contourinput):
        #initvalues
        rect=0
        widthheight=()
        anglestuff=[]
        potentialltarget=[]
        potentialrtarget=[]
        contourid=0
        pairlist=[]
        #find angles of contours
        for contour in contourinput:
            rect = cv2.minAreaRect(contour)
            angle=rect[2]
            box = cv2.boxPoints(rect)
            box = np.int0(box)
            cv2.drawContours(maskoutput,[box],0,(0,255,0),2)
            widthheight=widthheight+cv2.boundingRect(contour)
            if (angle<=-10.0 and angle>=-30.0):
                potentialltarget=potentialltarget+[contourid]
            elif (angle<=-50.0 and angle>=-75.0):
                potentialrtarget=potentialrtarget+[contourid]
            contourid+=1
        #PAIRING ALGORITHM
        for ltarget in potentialltarget:
            for rtarget in potentialrtarget:
                pairlist=pairlist+[(ltarget,rtarget)]
        for pair in pairlist:
            lcoordflt=cv2.minAreaRect(contourinput[pair[0]])[0]
            rcoordflt=cv2.minAreaRect(contourinput[pair[1]])[0]
            lcoord=(int(lcoordflt[0]),int(lcoordflt[1]))
            rcoord=(int(rcoordflt[0]),int(rcoordflt[1]))
            cv2.line(maskoutput,lcoord,rcoord,(255,0,0),2)
        return widthheight,pairlist

            
