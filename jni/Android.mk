LOCAL_PATH := $(call my-dir)

include $(LOCAL_PATH)/MarkerDetection/Android.mk

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include /Users/poly/workspace/OpenCV-2.4.6-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := simplear_marker_jni.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/MarkerDetection
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE     := simple_ar
LOCAL_SHARED_LIBRARIES += simple_ar_marker

include $(BUILD_SHARED_LIBRARY)
