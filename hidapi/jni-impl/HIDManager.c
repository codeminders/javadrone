
#include "jni-stubs/com_codeminders_hidapi_HIDManager.h"
#include "hidapi/hidapi.h"

#define DEVINFO_CLASS "com/codeminders/hidapi/HIDDeviceInfo"

static void setIntField(JNIEnv *env,
                        jclass cls,
                        jobject obj,
                        const char *name,
                        int val)
{
    jfieldID fid = (*env)->GetFieldID(env, cls, name, "I");
    (*env)->SetIntField(env, obj, fid, val);
}

static jobject createHIDDeviceInfo(JNIEnv *env, struct hid_device_info *dev)
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

    //private String path;
    //private String serial_number;
    //private String manufacturer_string;
    //private String product_string;
    
    setIntField(env, cls, result, "vendor_id", dev->vendor_id);
    setIntField(env, cls, result, "product_id", dev->product_id);
    setIntField(env, cls, result, "release_number", dev->release_number);
    setIntField(env, cls, result, "usage_page", dev->usage_page);
    setIntField(env, cls, result, "usage", dev->usage);
    setIntField(env, cls, result, "interface_number", dev->interface_number);
    
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


