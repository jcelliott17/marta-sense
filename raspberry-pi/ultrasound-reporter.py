import json
import uuid
import os
import thread
import random
from os import listdir
from os.path import isfile, join

import pyrebase

import RPi.GPIO as GPIO          #Import GPIO library
import time                      #Import time library

GPIO.setmode(GPIO.BCM)

SUCCESS_LED = 25

GPIO.setup(SUCCESS_LED,GPIO.OUT)

GPIO.output(SUCCESS_LED,GPIO.LOW)

script_dir = os.path.dirname(__file__)

configFile = open(os.path.join(script_dir, 'firebase-config.json'), 'r+')
config = json.load(configFile)

firebase = pyrebase.initialize_app(config)

db = firebase.database()

# storage = firebase.storage()

print("Listening for ultrasound signals")

this_car_num = 1;
this_car_id = "Car 1"

distance_threshold = 100

# ultrasoundRef = this_car_ref.child("ultrasound").child("distance-cm")

# print (ultrasoundRef.get().val())

carCount = db.child("cars/" + this_car_id + "/num-people").get().val()
if carCount == None:
    carCount = 0


TRIG = 23

ECHO = 24

TRIGGER_LED = 8

print "Distance Measurement In Progress"

GPIO.setup(TRIG,GPIO.OUT)

GPIO.setup(ECHO,GPIO.IN)

GPIO.output(TRIG, False)

GPIO.setup(TRIGGER_LED,GPIO.OUT)

print "Waiting For Sensor To Settle"


previousDistance = None

GPIO.output(SUCCESS_LED,GPIO.HIGH)

while True:
    time.sleep(0.3)

    GPIO.output(TRIG, True)

    time.sleep(0.00001)

    GPIO.output(TRIG, False)

    while GPIO.input(ECHO)==0:

      pulse_start = time.time()

    while GPIO.input(ECHO)==1:

      pulse_end = time.time()      

    pulse_duration = pulse_end - pulse_start

    distance = pulse_duration * 17150

    if previousDistance != None:
        if distance < distance_threshold and previousDistance > distance_threshold:
            carCount = carCount + 1
            db.child("cars/" + this_car_id + "/num-people").set(carCount)
            print "Entry Detected"
            print str(carCount) + " people"
            GPIO.output(TRIGGER_LED,GPIO.HIGH)
            time.sleep(1)
            GPIO.output(TRIGGER_LED,GPIO.LOW)

    print "Distance:",distance,"cm"

    previousDistance = distance

# ultrasoundRef.set(distance)

GPIO.cleanup()


