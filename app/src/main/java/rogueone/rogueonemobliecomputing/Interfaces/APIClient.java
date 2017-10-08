package rogueone.rogueonemobliecomputing.Interfaces;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rogueone.rogueonemobliecomputing.Models.LocationEntry;
import rogueone.rogueonemobliecomputing.Models.Trip;

public interface APIClient {
    //api/User/DiaryEntries
    @GET("/api/User/DiaryEntries")
    Call<List<LocationEntry>> getDiaryEntries();
    //api/User/Trips
    @GET("/api/User/getTrips")
    Call<List<Trip>> getTrips();
    //api/User/myTrips
    @GET("/api/User/getMyTrips")
    Call<List<String>> getTripNames();
    //api/User/PendingRequests
    @GET("/api/User/PendingRequests")
    Call<List<String>> getPendingRequests();
    //api/User/Friends
    @GET("/api/User/FriendList")
    Call<List<String>> getFriendList();
    //api/User/AppUsers
    @GET("/api/User/AppUsers")
    Call<List<String>> getAppUsers();
    //api/User/AddFriend
    @POST("/api/User/FriendRequest")
    Call<ResponseBody> SendRequest(@Body String Username);
    @POST("/api/User/ConfirmRequest")
    Call<ResponseBody> ConfirmRequest(@Body String Username);
    //api/User/Logout
    @POST("/api/Account/logout")
    Call<ResponseBody> logout();
    //api/User/createEntry
    @POST("/api/User/createEntry")
    Call<ResponseBody> createEntry(@Body LocationEntry entry);
    //api/User/createTrip
    @POST("/api/User/createTrip")
    Call<ResponseBody> createTrip(@Body Trip trip);
    //api/User/declineRequest
    @POST("/api/User/declineRequest")
    Call<ResponseBody> DeclineRequest(@Body String username);
    //api/User/DiaryEntries
    @GET("/api/User/UserDiaryEntries")
    Call<List<LocationEntry>> getDiaryEntries(@Query("Username") String Username);
    //api/User/Trips
    @GET("/api/User/GetUserTrips")
    Call<List<Trip>> getTrips(@Query("Username") String Username);
    @GET("/api/User/goIncognito")
    Call<ResponseBody> goIncognito();
    @GET("/api/User/getIncognito")
    Call<ResponseBody> getIncognito();
    @POST("/api/User/emergencyCheckIn")
    Call<ResponseBody> sendEmergencyCheckin(@Body LocationEntry entry);
    @GET("/api/User/getEmergencyCheckIn")
    Call<LocationEntry> getEmergencyCheckIn(@Query("Username") String username);
}
