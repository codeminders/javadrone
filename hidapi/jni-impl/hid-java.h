#define DEV_CLASS "com/codeminders/hidapi/HIDDevice"
#define DEVINFO_CLASS "com/codeminders/hidapi/HIDDeviceInfo"

void throwIOException(JNIEnv *env, hid_device *device);

// this call allocate buffer dynamically. return value should be released with free() routine  
char* convertToUTF8(const wchar_t *str); 