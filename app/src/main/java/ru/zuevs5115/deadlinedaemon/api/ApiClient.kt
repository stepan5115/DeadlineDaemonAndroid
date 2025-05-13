package ru.zuevs5115.deadlinedaemon.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

//Class to request/response to server
object ApiClient {
    //url for request/response
    private final const val BASE_URL = "http://192.168.10.51:8080/api/"

    //initial retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //add all services
    val authService: AuthService
        get() = retrofit.create(AuthService::class.java)
    val signUpService: SignUpService
        get() = retrofit.create(SignUpService::class.java)
    val getInfoService: GetInfoService
        get() = retrofit.create(GetInfoService::class.java)
    val completeAssignmentService: CompleteAssignmentService
        get() = retrofit.create(CompleteAssignmentService::class.java)
    val inCompleteAssignmentService: InCompleteAssignmentService
        get() = retrofit.create(InCompleteAssignmentService::class.java)
    val setNotificationStatusService: SetNotificationStatusService
        get() = retrofit.create(SetNotificationStatusService::class.java)
    val getAdminRightsService: GetAdminRightsService
        get() = retrofit.create(GetAdminRightsService::class.java)
    val setIntervalService: SetIntervalService
        get() = retrofit.create(SetIntervalService::class.java)
    val getAllSubjectsService: GetAllSubjectsService
        get() = retrofit.create(GetAllSubjectsService::class.java)
    val excludeSubjectService: ExcludeSubjectService
        get() = retrofit.create(ExcludeSubjectService::class.java)
    val includeSubjectService: IncludeSubjectService
        get() = retrofit.create(IncludeSubjectService::class.java)
    val getAllGroupsService: GetAllGroupsService
        get() = retrofit.create(GetAllGroupsService::class.java)
    val exitGroupService: ExitGroupService
        get() = retrofit.create(ExitGroupService::class.java)
    val enterGroupService: EnterGroupService
        get() = retrofit.create(EnterGroupService::class.java)
    val getAllGroupsIndependenceUserService: GetAllGroupsIndependenceUserService
        get() = retrofit.create(GetAllGroupsIndependenceUserService::class.java)
    val getAllSubjectsIndependenceUserService: GetAllSubjectsIndependenceUserService
        get() = retrofit.create(GetAllSubjectsIndependenceUserService::class.java)
    val getAllAssignmentsIndependenceUserService: GetAllAssignmentsIndependenceUserService
        get() = retrofit.create(GetAllAssignmentsIndependenceUserService::class.java)
}