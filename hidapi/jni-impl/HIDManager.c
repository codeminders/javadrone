
#include <stdlib.h>

#include "jni-stubs/com_codeminders_hidapi_HIDManager.h"
#include "hidapi/hidapi.h"
#include "hid-java.h"


static void setIntField(JNIEnv *env,
                        jclass cls,
                        jobject obj,
                        const char *name,
                        int val)
{
    jfieldID fid = (*env)->GetFieldID(env, cls, name, "I");
    (*env)->SetIntField(env, obj, fid, val);
}

static void setStringField(JNIEnv *env,
                           jclass cls,
                           jobject obj,
                           const char *name,
                           const char *val)
{
    jfieldID fid = (*env)->GetFieldID(env, cls, name, "Ljava/lang/String;");
    (*env)->SetObjectField(env, obj, fid,  val ? (*env)->NewStringUTF(env, val) : NULL);
}

static void setUStringField(JNIEnv *env,
                           jclass cls,
                           jobject obj,
                           const char *name,
                           const wchar_t *val)
{
    jfieldID fid = (*env)->GetFieldID(env, cls, name, "Ljava/lang/String;");

    if(val)
    {
        char *u8 = convertToUTF8(env, val);
        (*env)->SetObjectField(env, obj, fid, (*env)->NewStringUTF(env, u8));
        free(u8);
    }
    else
        (*env)->SetObjectField(env, obj, fid, NULL);
}

static jobject createHIDDeviceInfo(JNIEnv *env, jclass cls, struct hid_device_info *dev)
{
    jmethodID cid = (*env)->GetMethodID(env, cls, "<init>", "()V");
    if (cid == NULL) 
        return NULL; /* exception thrown. */ 

    jobject result = (*env)->NewObject(env, cls, cid);

    setIntField(env, cls, result, "vendor_id", dev->vendor_id);
    setIntField(env, cls, result, "product_id", dev->product_id);
    setIntField(env, cls, result, "release_number", dev->release_number);
    setIntField(env, cls, result, "usage_page", dev->usage_page);
    setIntField(env, cls, result, "usage", dev->usage);
    setIntField(env, cls, result, "interface_number", dev->interface_number);
    
    setStringField(env, cls, result, "path", dev->path);
    setUStringField(env, cls, result, "serial_number", dev->serial_number);
    setUStringField(env, cls, result, "manufacturer_string", dev->manufacturer_string);
    setUStringField(env, cls, result, "product_string", dev->product_string);

    return result;
}


JNIEXPORT jobjectArray JNICALL
Java_com_codeminders_hidapi_HIDManager_listDevices(JNIEnv *env, jclass cls)
{
	struct hid_device_info *devs, *cur_dev;
	
	devs = hid_enumerate(0x0, 0x0);
	if(devs == NULL)
	{
        throwIOException(env, NULL);
	    return NULL;
	}
	
	cur_dev = devs;
    int size=0;
	while(cur_dev)
    {
        size++;
		cur_dev = cur_dev->next;
	}

    jclass infoCls = (*env)->FindClass(env, DEVINFO_CLASS);
    if (infoCls == NULL) {
        return NULL; /* exception thrown */
    }
    jobjectArray result= (*env)->NewObjectArray(env, size, infoCls, NULL);
	cur_dev = devs;
    int i=0;
	while(cur_dev)
    {
        jobject x = createHIDDeviceInfo(env, infoCls, cur_dev);
        if(x == NULL)
            return; /* exception thrown */ 

        (*env)->SetObjectArrayElement(env, result, i, x);
        (*env)->DeleteLocalRef(env, x);
        i++;
		cur_dev = cur_dev->next;
	}
	hid_free_enumeration(devs);
	
    /* Free local references */
    (*env)->DeleteLocalRef(env, cls);
    
    return result;
}


