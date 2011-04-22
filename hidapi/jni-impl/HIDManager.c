
#include "jni-stubs/com_codeminders_hidapi_HIDManager.h"
#include "hidapi/hidapi.h"


static jobject createHIDDeviceInfo(JNIEnv *env,struct hid_device_info *dev)
{
    jclass cls = (*env)->FindClass(env, "com/codeminders/hidapi/HIDDeviceInfo");
    if (cls == NULL) {
        return NULL; /* exception thrown */
    }

    jmethodID cid = (*env)->GetMethodID(env, cls,
                                        "<init>", "()V");
    if (cid == NULL) {
        return NULL; /* exception thrown */
    }
    jobject result = (*env)->NewObject(env, cls, cid);

    //TODO: init fields
    
    /* Free local references */
    (*env)->DeleteLocalRef(env, cls);
    return result;
}


JNIEXPORT jobjectArray JNICALL
Java_com_codeminders_hidapi_HIDManager_listDevices(JNIEnv *env, jclass cls)
{
	struct hid_device_info *devs, *cur_dev;
	
	devs = hid_enumerate(0x0, 0x0);
	cur_dev = devs;	
	while(cur_dev)
    {
        jobject x = createHIDDeviceInfo(env, cur_dev);
		cur_dev = cur_dev->next;
	}
	hid_free_enumeration(devs);
    return 0;
}


