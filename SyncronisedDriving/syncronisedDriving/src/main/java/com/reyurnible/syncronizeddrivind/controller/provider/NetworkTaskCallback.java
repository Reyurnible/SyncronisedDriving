package com.reyurnible.syncronizeddrivind.controller.provider;public interface NetworkTaskCallback {	void onSuccessNetworkTask(final int taskId, final Object object);	void onFailedNetworkTask(final int taskId, final Object object);}