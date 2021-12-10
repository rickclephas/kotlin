#include <dlfcn.h>
#include <assert.h>
#include <pthread.h>

void* doTask(void* data)
{
	void* handle = dlopen("libso1.so", RTLD_LAZY);
	assert(handle);
	typedef int (*hello_t)();
	dlerror();
	hello_t hello1 = (hello_t)dlsym(handle, "hello1");
	assert(dlerror() == 0);

	hello1();
	return NULL;
}

int main()
{
	pthread_t tid;
	pthread_create(&tid, NULL, &doTask, NULL);
	pthread_join(tid, NULL);

	return 0;
}
