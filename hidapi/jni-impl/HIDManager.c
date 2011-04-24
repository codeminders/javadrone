
#include <iconv.h>
#include <stdlib.h>

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

static void setStringField(JNIEnv *env,
                           jclass cls,
                           jobject obj,
                           const char *name,
                           const char *val)
{
    jfieldID fid = (*env)->GetFieldID(env, cls, name, "Ljava/lang/String;");
    (*env)->SetObjectField(env, obj, fid,  (*env)->NewStringUTF(env, val));
}

static void setUStringField(JNIEnv *env,
                           jclass cls,
                           jobject obj,
                           const char *name,
                           const wchar_t *val)
{
    jfieldID fid = (*env)->GetFieldID(env, cls, name, "Ljava/lang/String;");

    iconv_t cd = iconv_open ("UTF-8", "WCHAR_T");
    if (cd == (iconv_t) -1)
    {
        /* Something went wrong.  */
        //TODO: error handling
    }
    size_t len = wcslen(val);
    size_t ulen = len*sizeof(wchar_t);
    char *uval = (char *)val;
    
    size_t u8l;
    char *u8 = malloc(len*6+1);
    char *u8p = u8;
    iconv(cd,
          &uval, &ulen,
          &u8p, &u8l
    );
    *u8p='\0';
    
    (*env)->SetObjectField(env, obj, fid, (*env)->NewStringUTF(env, u8));
    free(u8);
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

    jclass arrCls = (*env)->FindClass(env, DEVINFO_CLASS);
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


