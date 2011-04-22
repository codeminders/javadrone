
#include "jni-stubs/com_codeminders_hidapi_HIDManager.h"
#include "hidapi/hidapi.h"

#define DEVINFO_CLASS "com/codeminders/hidapi/HIDDeviceInfo"

static jobject createHIDDeviceInfo(JNIEnv *env,struct hid_device_info *dev)
{
    jclass cls = (*env)->FindClass(env, DEVINFO_CLASS);
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
    int size=0;
	while(cur_dev)
    {
        size++;
		cur_dev = cur_dev->next;
	}

    jclass arrCls = (*env)->FindClass(env, "[" DEVINFO_CLASS);
    if (arrCls == NULL) {
        return NULL; /* exception thrown */
    }
    jobjectArray result= (*env)->NewObjectArray(env, size, arrCls,
                                                NULL);
	cur_dev = devs;
    int i=0;
	while(cur_dev)
    {
        jobject x = createHIDDeviceInfo(env, cur_dev);
        (*env)->SetObjectArrayElement(env, result, i, x);
        (*env)->DeleteLocalRef(env, x);
        i++;
		cur_dev = cur_dev->next;
	}
	hid_free_enumeration(devs);
    return result;
}


