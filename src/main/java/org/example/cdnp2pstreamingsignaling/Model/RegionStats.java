package org.example.cdnp2pstreamingsignaling.Model;

public class RegionStats {
    private int userCount;
    private double totalUploadBandwith;

    public synchronized void adduser(double userUploadBandwith) {
        userCount++;
        totalUploadBandwith += userUploadBandwith;
    }

    public synchronized void deleteUser(double userUploadBandwith) {
        userCount = Math.max(0, userCount - 1);
        totalUploadBandwith = Math.max(0, totalUploadBandwith - userUploadBandwith);

    }

    public synchronized double getAvarege(){
        if (userCount == 0){
            return 0.0;
        }
        return totalUploadBandwith/userCount;
    }

}
