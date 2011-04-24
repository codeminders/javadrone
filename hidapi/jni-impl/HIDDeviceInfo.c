
#include "jni-stubs/com_codeminders_hidapi_HIDDeviceInfo.h"
#include "hidapi/hidapi.h"
#include "hid-java.h"

JNIEXPORT jobject JNICALL Java_com_codeminders_hidapi_HIDDeviceInfo_open
  (JNIEnv *env, jobject obj)
{
    jclass thiscls = (*env)->FindClass(env, DEVINFO_CLASS);
    if (!thiscls)
        return NULL;
    
    jfieldID path_field_id = (*env)->GetFieldID(env, thiscls, "path", "Ljava/lang/String;");
    jstring jpathstr = (*env)->GetObjectField(env, obj, path_field_id);

    const jbyte *jpathbytes = (*env)->GetStringUTFChars(env, jpathstr, NULL);
    if(!jpathbytes)
        return NULL;
    
    hid_device *dev = hid_open_path(jpathbytes);
    (*env)->ReleaseStringUTFChars(env, jpathstr, jpathbytes); 
    if(!dev)
        return NULL;
    
    jlong peer = (jlong)dev;
    // Construct and return object
    jclass cls = (*env)->FindClass(env, DEV_CLASS);
    if (cls == NULL) {
        return NULL; /* exception thrown */
    }

    jmethodID cid = (*env)->GetMethodID(env, cls,
                                        "<init>", "(J)V");
    if (cid == NULL) {
        return NULL; /* exception thrown */
    }
    return (*env)->NewObject(env, cls, cid, peer);
}
