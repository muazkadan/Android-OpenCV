# What is OpenCV:
OpenCV stands for Open Source Computer Vision Library, it’s an open source image processing and machine learning library. The library is available for multiple platforms such as Windows, Linux, Android and IOS. 

# Content of Project:
This project was created for a university course. In this project I used OpenCV library to create a simple Android app with some image processing functionalities.   
Version 3.4.7 of the library was used because the latest versions had some compatibility problems with Android.  

![Screenshot](https://i.ibb.co/1R8gYW1/1.jpg)

JavaCameraView were used as camera in the project. SetOnTouchListener was used to detect the motion on the camera view to switch between filters. Filtering operations were done over the Mat object returned by the camera. The returned mat object is n-dimensional dense array. 

![Screenshot](https://i.ibb.co/c8Mcdpd/2.jpg)

To apply the filters Imgproc class were used, which is a built-in openCV class that is used for image processing, the class has a lot of functions such as cvtColor() that is used to convert between color spaces, this function takes 3 parameters the first one is source Mat object, second one is destination Mat object and the third one is constant from  Imgproc class which used to specify the two color spaces we converting between. 

The class has another functions like Canny(). Canny is an edge detection operator that is used to detect a wide range of edges in images. The function takes 4 parameters the first one is source Mat object, second one is destination Mat object, the third and forth ones are thresholding values.

![Screenshot](https://i.ibb.co/J3B74D0/3.jpg) ![Screenshot](https://i.ibb.co/xHhMKrL/4.png)


There is also applyColorMap() function which takes the first and the second parameters like the other two functions and a constant as the third parameter which specifies the color map to be applied. 
