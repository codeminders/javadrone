
#


CFLAGS=-I.. -I/System/Library/Frameworks/JavaVM.framework/Headers 
OBJS=HIDManager.o HIDDeviceInfo.o HIDDevice.o hid-java.o
EXTRAOBJS=../mac/hid.o
SHAREDLIB=libhidapi-jni.jnilib
SHAREDLIBVER=1.0
LIBS=-framework IOKit -framework CoreFoundation -l iconv

all: $(SHAREDLIB)

$(OBJS): hid-java.h

$(SHAREDLIB): $(OBJS)
	gcc -dynamiclib -current_version $(SHAREDLIBVER) $(OBJS) $(EXTRAOBJS) $(LIBS) -o $(SHAREDLIB)

clean:
	rm -f $(OBJS) $(SHAREDLIB)