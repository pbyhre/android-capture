# android-capture
Android biometric capture library

Android library for using capture devices for things like camera and audio capture that are used in biometrics.  

To add a new capture device
1. Extend com.openbiometrix.capture.CaptureDevice to implement the core functioning of the device.  
2. Implement a View that implements the com.openbiometrix.capture.ui.CaptureView interface.  This interface will have a buffer that is used to transmit a stream of data from the device to the CaptureView as the preview is happening.
