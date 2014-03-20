LOCAL_PATH_ORIG := $(LOCAL_PATH)
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include /Users/poly/workspace/OpenCV-2.4.6-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES = CameraCalibration.cpp GeometryTypes.cpp Marker.cpp MarkerDetector.cpp TinyLA.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_CFLAGS    := -Werror -O3 -ffast-math
LOCAL_LDLIBS     += -llog -ldl
LOCAL_ARM_NEON := true
LOCAL_MODULE := simple_ar_marker
include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(LOCAL_PATH_ORIG)