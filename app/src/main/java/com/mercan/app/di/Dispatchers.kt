package com.mercan.app.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Uygulama yaşadıkça süren işler (alarm ve iş alıcıları) için kapsam. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope
