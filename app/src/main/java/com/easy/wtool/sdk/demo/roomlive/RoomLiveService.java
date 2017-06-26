package com.easy.wtool.sdk.demo.roomlive;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.easy.wtool.sdk.MessageEvent;
import com.easy.wtool.sdk.OnMessageListener;
import com.easy.wtool.sdk.WToolSDK;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by TK on 2017-02-23.
 */

public class RoomLiveService extends Service {
    private static final String LOG_TAG = "javahook";
    WToolSDK wToolSDK = null;
    JSONArray jsonArraySlaves = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RoomLiveParams roomLiveParams = new RoomLiveParams(getBaseContext());
        final String masterids = roomLiveParams.getMasterChatroomIds();
        final String fromids = roomLiveParams.getMasterSpeakers();
        String slaveids = roomLiveParams.getSlaveChatroomIds();
        if(slaveids.equals(""))
        {
            slaveids = "[]";
        }


        try
        {

            JSONArray jsonArrayMaster = new JSONArray(masterids);
            for(int i=0;i<jsonArrayMaster.length();i++)
            {
                if(slaveids.indexOf(jsonArrayMaster.getString(i))>=0)
                {
                    slaveids = slaveids.replaceAll(jsonArrayMaster.getString(i),"");
                }
            }
            jsonArraySlaves = new JSONArray(slaveids);

        }
        catch (Exception e)
        {
            jsonArraySlaves = null;
        }
        Log.d(LOG_TAG,"RoomLiveService.onStartCommand: "+roomLiveParams.getAppId()+","+roomLiveParams.getAuthCode());
        wToolSDK.init(roomLiveParams.getAppId(),roomLiveParams.getAuthCode());
        //处理消息 回调的Handler
        final Handler messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


                MessageEvent event = (MessageEvent) msg.obj;

                //editContent.append("message: " + event.getTalker() + "," +wToolSDK.decodeValue(event.getContent()) + "\n");
                super.handleMessage(msg);
            }
        };
        wToolSDK.setOnMessageListener(new OnMessageListener() {
            @Override
            public void messageEvent(MessageEvent event) {
                String content = (event.getContent());
                try
                {
                    JSONObject jsonObject = new JSONObject(content);
                    content = wToolSDK.decodeValue(jsonObject.getString("content"));

                    if(jsonArraySlaves!=null && masterids.indexOf("\""+event.getTalker()+"\"")>=0) {
                        //wToolSDK.transferMessage(jsonArraySlaves.toString(), event.getMsgType(),jsonObject.getString("msgid"));
                        for(int i=0;i<jsonArraySlaves.length();i++) {
                            JSONObject jsonTask = new JSONObject();
                            jsonTask.put("type", 12);
                            jsonTask.put("taskid", System.currentTimeMillis());
                            jsonTask.put("content", new JSONObject());
                            jsonTask.getJSONObject("content").put("talker",jsonArraySlaves.getString(i));
                            jsonTask.getJSONObject("content").put("msgtype",  event.getMsgType());
                            jsonTask.getJSONObject("content").put("msgid", jsonObject.getString("msgid"));

                            content = wToolSDK.sendTask(jsonTask.toString());
                        }
                    }
                }
                catch (Exception e)
                {

                }
                Log.d(LOG_TAG, "on message: " + event.getTalker()+"["+event.getFrom()+"]" + "," +content);



            }
        });
        try {
            //设置消息过滤
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            //jsonArray.put(1);
            //只接收群消息
            jsonArray.put(2);
            jsonObject.put("talkertypes", jsonArray);
            //消息来源过滤
            //只接收主播群的消息
            String talkers = roomLiveParams.getMasterChatroomIds();
            if(talkers.equals(""))
            {
                talkers = "[]";
            }
            jsonObject.put("froms", new JSONArray(talkers));
            //设置消息发送者过滤
            //只接收主播群中主讲人的消息
            String froms = roomLiveParams.getMasterSpeakers();
            if(froms.equals(""))
            {
                froms = "[]";
            }
            jsonObject.put("froms", new JSONArray(froms));
            //设置消息类型过滤
            String msgTypes = roomLiveParams.getTransferMessages();
            if(msgTypes.equals(""))
            {
                msgTypes = "[0]";
            }

            jsonObject.put("msgtypes", new JSONArray(msgTypes));
            //设置消息关键字过滤
            jsonObject.put("msgfilters", new JSONArray());
            String result = wToolSDK.startMessageListener(jsonObject.toString());
            jsonObject = new JSONObject(result);
            if (jsonObject.getInt("result") == 0) {
                //buttonStartMessage.setTag(1);
                //buttonStartMessage.setText("停止监听消息");
                Log.d(LOG_TAG,"启动直播服务成功");
                Toast.makeText(this, "启动直播服务成功", Toast.LENGTH_LONG).show();

            }
            else
            {
                Log.d(LOG_TAG,"启动直播服务失败>>"+jsonObject.getString("errmsg"));
                Toast.makeText(this, "启动直播服务失败>>"+jsonObject.getString("errmsg"), Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "err", e);
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        //Toast.makeText(this, "My Service created", Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG,"RoomLiveService.onCreate");
        wToolSDK = new WToolSDK();

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG,"RoomLiveService.onDestroy");


        Toast.makeText(this, "直播服务已关闭", Toast.LENGTH_LONG).show();
    }
}
