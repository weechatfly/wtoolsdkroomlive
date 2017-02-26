package com.easy.wtool.sdk.demo.roomlive;

import android.content.Context;

/**
 * Created by TK on 2017-02-23.
 */

public class RoomLiveParams {
    private static final String SECTION = "wtoolroomliveparams";
    String authCode = "";
    String masterChatroomIds,masterChatroomNames;
    String masterSpeakers,masterSpeakerNames;
    String slaveChatroomIds,slaveChatroomNames;
    String transferMessages;

    ConfigUtils configUtils;
    public RoomLiveParams(Context context)
    {
        configUtils = new ConfigUtils(context);
    }


    public String getMasterChatroomIds() {
        return configUtils.get(SECTION,"MasterChatroomIds","");
    }

    public void setMasterChatroomIds(String masterChatroomIds) {
        this.masterChatroomIds = masterChatroomIds;
        configUtils.save(SECTION,"MasterChatroomIds",masterChatroomIds);
    }

    public String getMasterChatroomNames() {
        String names = configUtils.get(SECTION,"MasterChatroomNames","");
        if(names.length()>50)
        {
            names = names.substring(0,50)+"...";
        }
        return names;
    }

    public void setMasterChatroomNames(String masterChatroomNames) {
        this.masterChatroomNames = masterChatroomNames;
        configUtils.save(SECTION,"MasterChatroomNames",masterChatroomNames);
    }


    public String getMasterSpeakers() {
        return configUtils.get(SECTION,"MasterSpeakers","");
    }

    public void setMasterSpeakers(String masterSpeakers) {
        this.masterSpeakers = masterSpeakers;
        configUtils.save(SECTION,"MasterSpeakers",masterSpeakers);
    }

    public String getMasterSpeakerNames() {
        String names = configUtils.get(SECTION,"MasterSpeakerNames","");
        if(names.length()>50)
        {
            names = names.substring(0,50)+"...";
        }
        return names;
    }

    public void setMasterSpeakerNames(String masterSpeakerNames) {
        this.masterSpeakerNames = masterSpeakerNames;
        configUtils.save(SECTION,"MasterSpeakerNames",masterSpeakerNames);
    }




    public String getSlaveChatroomIds() {
        return configUtils.get(SECTION,"SlaveChatroomIds","");
    }

    public void setSlaveChatroomIds(String slaveChatroomIds) {
        this.slaveChatroomIds = slaveChatroomIds;
        configUtils.save(SECTION,"SlaveChatroomIds",slaveChatroomIds);
    }

    public String getSlaveChatroomNames() {
        String names = configUtils.get(SECTION,"SlaveChatroomNames","");
        if(names.length()>50)
        {
            names = names.substring(0,50)+"...";
        }
        return names;
    }

    public void setSlaveChatroomNames(String slaveChatroomNames) {
        this.slaveChatroomNames = slaveChatroomNames;
        configUtils.save(SECTION,"SlaveChatroomNames",slaveChatroomNames);
    }

    public String getTransferMessages() {
        return configUtils.get(SECTION,"TransferMessages","[1,3,34,43,62]");
    }

    public void setTransferMessages(String transferMessages) {
        this.transferMessages = transferMessages;
        configUtils.save(SECTION,"TransferMessages",transferMessages);
    }





    public String getAuthCode() {
        return configUtils.get(SECTION,"authcode","");
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
        configUtils.save(SECTION,"authcode",authCode);
    }
}
