#ifndef __HID_JAVA_H__
#define __HID_JAVA_H__

#define DEV_CLASS "com/codeminders/hidapi/HIDDevice"
#define DEVINFO_CLASS "com/codeminders/hidapi/HIDDeviceInfo"

#ifdef __cplusplus
extern "C" {
#endif

void throwIOException(JNIEnv *env, hid_device *device);

/* this call allocate buffer dynamically. return value should be
   released with free() routine */
char* convertToUTF8(JNIEnv *env, const wchar_t *str); 

#ifdef __cplusplus
}
#endif

#endif // __HID_JAVA_H__
