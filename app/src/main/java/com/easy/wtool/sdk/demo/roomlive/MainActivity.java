package com.easy.wtool.sdk.demo.roomlive;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.easy.wtool.sdk.WToolSDK;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "javahook";
    private static String DEF_MASTERCHATROOMS = "主播群(点击选择)";
    private static String DEF_MASTERSPEAKERS = "主讲人(点击选择)";
    private static String DEF_SLAVECHATROOMS = "从播群(点击选择)";
    Context mContext;
    // Used to load the 'native-lib' library on application startup.

    private ConfigUtils configUtils;
    private List<String> selectedWxIdIndex = new ArrayList<String>();

    private ListView listViewSelectDialog = null;
    private boolean [] checks;
    private AlertDialog selectDialog = null;
    private int masterWxIdIndex = 0;
    private boolean isInited = false;
    EditText editAppId;
    EditText editAuthCode;
    WToolSDK wToolSDK = new WToolSDK();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        final RoomLiveParams roomLiveParams = new RoomLiveParams(mContext);

        //this.setTitle(this.getTitle() + " - V" + wToolSDK.getVersion());

        configUtils = new ConfigUtils(this);
        wToolSDK.encodeValue("1");
        //Log.d(LOG_TAG,"encode: "+wToolSDK.encodeValue("http://www.baidu.com"));
        TextView textViewPrompt = (TextView) findViewById(R.id.textViewPrompt);
        textViewPrompt.setClickable(true);
        textViewPrompt.setMovementMethod(LinkMovementMethod.getInstance());
        String prompt = "<b>本软件基于<a href=\"http://repo.xposed.info/module/com.easy.wtool\">微控工具xp模块-开发版[微信(wechat)二次开发模块]</a>"
                +"开发，使用前请确认模块已经安装，模块最低版本：1.0.1.102[1.0.1.102-开发版]</b>";
        textViewPrompt.setText(Html.fromHtml(prompt));
        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
        final Button buttonRoomLive = (Button) findViewById(R.id.buttonRoomLive);

        final CheckBox chkMessageText = (CheckBox)findViewById(R.id.chkMessageText);
        final CheckBox chkMessageImage = (CheckBox)findViewById(R.id.chkMessageImage);
        final CheckBox chkMessageVoice = (CheckBox)findViewById(R.id.chkMessageVoice);
        final CheckBox chkMessageVideo = (CheckBox)findViewById(R.id.chkMessageVideo);
        final CheckBox chkMultiText = (CheckBox)findViewById(R.id.chkMultiText);
        String msgTypes = roomLiveParams.getTransferMessages();
        //Log.d(LOG_TAG,"msgtypes: "+msgTypes);
        if(!msgTypes.equals(""))
        {
            try
            {
                JSONArray jsonArray = new JSONArray(msgTypes);
                for(int i=0;i<jsonArray.length();i++)
                {
                    if(jsonArray.getInt(i)==1)
                    {
                        chkMessageText.setChecked(true);
                    }
                    else if(jsonArray.getInt(i)==3)
                    {
                        chkMessageImage.setChecked(true);
                    }
                    else if(jsonArray.getInt(i)==34)
                    {
                        chkMessageVoice.setChecked(true);
                    }
                    else if(jsonArray.getInt(i)==43||jsonArray.getInt(i)==62)
                    {
                        chkMessageVideo.setChecked(true);
                    }
                    else if(jsonArray.getInt(i)==49)
                    {
                        chkMultiText.setChecked(true);
                    }
                }
            }
            catch (Exception e)
            {

            }
        }
        final TextView labelMasterChatroomIds = (TextView) findViewById(R.id.labelMasterChatroomIds);
        labelMasterChatroomIds.setText(DEF_MASTERCHATROOMS+(roomLiveParams.getMasterChatroomIds().equals("")?"":":"+roomLiveParams.getMasterChatroomNames()));
        final TextView labelMasterSpeakers = (TextView) findViewById(R.id.labelMasterSpeakers);
        labelMasterSpeakers.setText(DEF_MASTERSPEAKERS+(roomLiveParams.getMasterSpeakers().equals("")?"":":"+roomLiveParams.getMasterSpeakerNames()));
        final TextView labelSlaveChatroomIds = (TextView) findViewById(R.id.labelSlaveChatroomIds);
        labelSlaveChatroomIds.setText(DEF_SLAVECHATROOMS+(roomLiveParams.getSlaveChatroomIds().equals("")?"":":"+roomLiveParams.getSlaveChatroomNames()));


        editAppId = (EditText) findViewById(R.id.editAppId);
        editAuthCode = (EditText) findViewById(R.id.editAuthCode);
        //final TextView edtContent = (TextView) findViewById(R.id.edtContent);
        //edtContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        editAppId.setText(configUtils.get(ConfigUtils.KEY_APPID, ""));
        editAuthCode.setText(configUtils.get(ConfigUtils.KEY_AUTHCODE, ""));
        initSDK();
        editAppId.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {

                isInited = false;

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        editAuthCode.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {

                isInited = false;

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        if (ServiceUtils.isServiceRunning(mContext,RoomLiveService.class.getName())) {
            buttonRoomLive.setText("关闭直播");
            buttonRoomLive.setTag(1);
        } else {
            buttonRoomLive.setTag(0);
            buttonRoomLive.setText("启动直播");
        }


        buttonRoomLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonRoomLive.getTag().equals(0)) {
                    if(!initSDK())
                    {
                        return;
                    }

                    if (roomLiveParams.getMasterChatroomIds().equals("")) {
                        Toast.makeText(mContext, "请设置主播群！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (roomLiveParams.getMasterSpeakers().equals("")) {
                        Toast.makeText(mContext, "请设置主讲人！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (roomLiveParams.getSlaveChatroomIds().equals("")) {
                        Toast.makeText(mContext, "请设置从播群！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent i = new Intent(mContext, RoomLiveService.class);
                    String msgTypes = "";
                    if(chkMessageText.isChecked())
                    {
                        if(!msgTypes.equals(""))
                        {
                            msgTypes += ",";
                        }
                        msgTypes += "1";
                    }
                    if(chkMessageImage.isChecked())
                    {
                        if(!msgTypes.equals(""))
                        {
                            msgTypes += ",";
                        }
                        msgTypes += "3";
                    }
                    if(chkMessageVoice.isChecked())
                    {
                        if(!msgTypes.equals(""))
                        {
                            msgTypes += ",";
                        }
                        msgTypes += "34";
                    }
                    if(chkMessageVideo.isChecked())
                    {
                        if(!msgTypes.equals(""))
                        {
                            msgTypes += ",";
                        }
                        msgTypes += "43,62";
                    }
                    if(chkMultiText.isChecked())
                    {
                        if(!msgTypes.equals(""))
                        {
                            msgTypes += ",";
                        }
                        msgTypes += "49";
                    }
                    if(!msgTypes.equals(""))
                    {
                        msgTypes = "["+msgTypes+"]";
                    }
                    //Log.d(LOG_TAG,"msgtypes: "+msgTypes);
                    roomLiveParams.setAppId(editAppId.getText().toString());
                    roomLiveParams.setAuthCode(editAuthCode.getText().toString());
                    roomLiveParams.setTransferMessages(msgTypes);
                    startService(i);
                    //Log.d(LOG_TAG,"result: "+i.getIntExtra("result",0));
                    buttonRoomLive.setText("关闭直播");
                    buttonRoomLive.setTag(1);
                }
                else
                {
                    stopService(new Intent(mContext, RoomLiveService.class));
                    buttonRoomLive.setText("启动直播");
                    buttonRoomLive.setTag(0);
                }
            }
        });
        labelMasterChatroomIds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!initSDK())
                {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                //builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("选择主播群");
                String content = "";
                //content = wToolSDK.getChatrooms(0, 0,false);
                try {
                    JSONObject jsonTask = new JSONObject();
                    jsonTask.put("type", 6);
                    jsonTask.put("taskid", System.currentTimeMillis());
                    jsonTask.put("content", new JSONObject());
                    jsonTask.getJSONObject("content").put("pageindex", 0);
                    jsonTask.getJSONObject("content").put("pagecount", 0);
                    jsonTask.getJSONObject("content").put("ismembers", 0);

                    content = wToolSDK.sendTask(jsonTask.toString());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.e(LOG_TAG,"err",e);
                }

                String text = "";
                String masterids = roomLiveParams.getMasterChatroomIds();
                String wxid;
                try {


                    final JSONObject jsonObject = new JSONObject(content);
                    if (jsonObject.getInt("result") == 0) {
                        final JSONArray jsonArray = jsonObject.getJSONArray("content");
                        if (jsonArray.length() > 0) {
                            final String[] friends = new String[jsonArray.length()];
                            for (int i = 0; i < jsonArray.length(); i++) {
                                wxid = wToolSDK.decodeValue(jsonArray.getJSONObject(i).getString("wxid"));
                                if(masterids.indexOf("\""+wxid+"\"")>=0)
                                {
                                    masterWxIdIndex = i;
                                }
                                friends[i] = wToolSDK.decodeValue(jsonArray.getJSONObject(i).getString("nickname"));

                                if(friends[i].equals("") && jsonArray.getJSONObject(i).has("displayname")) {
                                    friends[i] = wToolSDK.decodeValue(jsonArray.getJSONObject(i).getString("displayname"));
                                    if(friends[i].length()>20) {
                                        friends[i] = friends[i].substring(0, 20) + "...";
                                    }
                                }

                            }



                            builder.setSingleChoiceItems(friends, masterWxIdIndex, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    masterWxIdIndex = which;
                                }
                            });
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        roomLiveParams.setMasterChatroomIds("[\""+wToolSDK.decodeValue(jsonArray.getJSONObject(masterWxIdIndex).getString("wxid"))+"\"]");
                                        roomLiveParams.setMasterChatroomNames(friends[masterWxIdIndex]);
                                        labelMasterChatroomIds.setText(DEF_MASTERCHATROOMS + "：" + friends[masterWxIdIndex]);
                                        Toast.makeText(mContext, "设置已修改需要重启直播才会生效！", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {

                                    }

                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                        } else {
                            text = "无群可选择";
                        }
                    } else {
                        text = jsonObject.getString("errmsg");
                    }
                } catch (Exception e) {
                    text = "解析结果失败";
                    Log.e(LOG_TAG, "jsonerr", e);
                }
                if (text.length() > 0) {
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }


            }

        });
        View.OnClickListener onSelectWxIdClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!initSDK())
                {
                    return;
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                //获取组件的资源id
                int id = v.getId();
                final boolean isselectspeakers = id==R.id.labelMasterSpeakers;
                builder.setTitle(isselectspeakers?"选择主讲人":"选择从播群");
                String content = "";
                String text = "";
                String wxid;
                String masterids = roomLiveParams.getMasterChatroomIds();
                if(masterids.equals(""))
                {
                    Toast.makeText(mContext, "请设置主播群！", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    final JSONArray jsonArray = new JSONArray();
                    //content = wToolSDK.getChatrooms(0, 0,isselectspeakers);
                    try {
                        JSONObject jsonTask = new JSONObject();
                        jsonTask.put("type", 6);
                        jsonTask.put("taskid", System.currentTimeMillis());
                        jsonTask.put("content", new JSONObject());
                        jsonTask.getJSONObject("content").put("pageindex", 0);
                        jsonTask.getJSONObject("content").put("pagecount", 0);
                        jsonTask.getJSONObject("content").put("ismembers", isselectspeakers?1:0);

                        content = wToolSDK.sendTask(jsonTask.toString());
                    }
                    catch(Exception e)
                    {

                    }
                    final JSONObject jsonObject = new JSONObject(content);
                    if (jsonObject.getInt("result") == 0) {
                        //edtContent.setText(content);
                        JSONArray jsonArray1 = jsonObject.getJSONArray("content");
                        if (jsonArray1.length() > 0) {
                            for(int i=0;i<jsonArray1.length();i++) {
                                wxid = wToolSDK.decodeValue(jsonArray1.getJSONObject(i).getString("wxid"));
                                if(isselectspeakers)
                                {

                                    if(masterids.indexOf("\""+wxid+"\"")>=0)
                                    {
                                        if(jsonArray1.getJSONObject(i).has("members")) {
                                            JSONArray jsonArray2 = jsonArray1.getJSONObject(i).getJSONArray("members");
                                            for (int j = 0; j < jsonArray2.length(); j++) {
                                                jsonArray.put(jsonArray2.getJSONObject(j));
                                            }
                                        }
                                    }
                                }
                                else {
                                    if(masterids.indexOf("\""+wxid+"\"")<0) {
                                        jsonArray.put(jsonArray1.getJSONObject(i));
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        text = jsonObject.getString("errmsg");
                    }




                    if(text.length()==0) {
                        checks = new boolean[jsonArray.length()+1];
                        selectedWxIdIndex.clear();


                        if (jsonArray.length() > 0) {
                            final String[] friends = new String[jsonArray.length()+1];

                            String saveids = "";

                            if(isselectspeakers)
                            {
                                saveids = roomLiveParams.getMasterSpeakers();
                            }
                            else
                            {
                                saveids = roomLiveParams.getSlaveChatroomIds();
                            }
                            if(saveids.equals(""))
                            {
                                saveids = "[]";
                            }
                            friends[0] = "全选";
                            for (int i = 0; i < jsonArray.length(); i++) {
                                wxid = wToolSDK.decodeValue(jsonArray.getJSONObject(i).getString("wxid"));
                                checks[i+1] = (saveids.indexOf("\""+wxid+"\"")>=0);
                                friends[i + 1] = wToolSDK.decodeValue(jsonArray.getJSONObject(i).getString("nickname"));
                                if (friends[i + 1].equals("")) {
                                    if (jsonArray.getJSONObject(i).has("displayname")) {
                                        friends[i + 1] = wToolSDK.decodeValue(jsonArray.getJSONObject(i).getString("displayname"));
                                        if (friends[i + 1].length() > 20) {
                                            friends[i + 1] = friends[i + 1].substring(0, 20) + "...";
                                        }
                                    }
                                }
                                if(checks[i+1])
                                {
                                    selectedWxIdIndex.add(String.valueOf(i));
                                }
                            }


                            final DialogInterface.OnClickListener onOkClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        if(selectedWxIdIndex.size()>0) {
                                            String nicknames = "";
                                            String wxids = "[";
                                            for (int i = 0; i < selectedWxIdIndex.size(); i++) {
                                                if (i > 0) {
                                                    wxids += ",";
                                                    nicknames += ",";
                                                }
                                                wxids += "\"" + wToolSDK.decodeValue(jsonArray.getJSONObject(Integer.parseInt(selectedWxIdIndex.get(i))).getString("wxid")) + "\"";
                                                nicknames += friends[Integer.parseInt(selectedWxIdIndex.get(i))+1];
                                            }
                                            wxids += "]";


                                            if(isselectspeakers)
                                            {
                                                roomLiveParams.setMasterSpeakers(wxids);
                                                roomLiveParams.setMasterSpeakerNames(nicknames);
                                            }
                                            else
                                            {
                                                roomLiveParams.setSlaveChatroomIds(wxids);
                                                roomLiveParams.setSlaveChatroomNames(nicknames);
                                            }
                                            if (nicknames.length() > 50) {
                                                nicknames = nicknames.substring(0, 50) + "...";
                                            }
                                            if(isselectspeakers) {
                                                labelMasterSpeakers.setText(DEF_MASTERSPEAKERS  + "：" + nicknames);
                                            }
                                            else
                                            {
                                                labelSlaveChatroomIds.setText(DEF_SLAVECHATROOMS + "：" + nicknames);
                                            }
                                            Toast.makeText(mContext, "设置已修改需要重启直播才会生效！", Toast.LENGTH_LONG).show();
                                        }

                                    } catch (Exception e) {
                                        Log.e(LOG_TAG,"err",e);
                                    }

                                }
                            };
                            final DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener = new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                                    if(which==0)
                                    {
                                        if(isChecked) {
                                            SparseBooleanArray sb;
                                            sb = listViewSelectDialog.getCheckedItemPositions();
                                            for (int i = 1; i < checks.length; i++) {
                                                if (sb.get(i) == false) {
                                                    listViewSelectDialog.setItemChecked(i, isChecked);
                                                }


                                                if (!selectedWxIdIndex.contains(String.valueOf(i-1))) {
                                                    selectedWxIdIndex.add(String.valueOf(i-1));
                                                }


                                            }
                                        }
                                        else
                                        {
                                            SparseBooleanArray sb;
                                            sb = listViewSelectDialog.getCheckedItemPositions();
                                            for (int i = 1; i < checks.length; i++) {
                                                if (sb.get(i) == true) {
                                                    listViewSelectDialog.setItemChecked(i, isChecked);
                                                }
                                                if (selectedWxIdIndex.contains(String.valueOf(i-1))) {
                                                    selectedWxIdIndex.remove(String.valueOf(i-1));
                                                }
                                            }
                                            //下面这个必须加，不然如果是单独勾选的，全不选时取消不了
                                            for (int i = 0; i < checks.length; i++) {
                                                checks[i] = false;
                                            }
                                            listViewSelectDialog.clearChoices();
                                        }


                                    }
                                    else {
                                        checks[which] = isChecked;
                                        //Log.d(LOG_TAG,"select "+which+","+isChecked);
                                        if (isChecked) {

                                            if (!selectedWxIdIndex.contains(String.valueOf(which-1))) {
                                                selectedWxIdIndex.add(String.valueOf(which-1));
                                            }

                                        } else {

                                            if (selectedWxIdIndex.contains(String.valueOf(which-1))) {
                                                selectedWxIdIndex.remove(String.valueOf(which-1));
                                            }

                                        }
                                        SparseBooleanArray sb;
                                        sb = listViewSelectDialog.getCheckedItemPositions();
                                        if(selectedWxIdIndex.size()==jsonArray.length())
                                        {
                                            if (sb.get(0) == false) {
                                                listViewSelectDialog.setItemChecked(0, true);
                                            }
                                            checks[0] = true;
                                        }
                                        else if(selectedWxIdIndex.size()==0)
                                        {
                                            if (sb.get(0) == true) {
                                                listViewSelectDialog.setItemChecked(0, false);
                                            }
                                            checks[0] = false;
                                        }
                                    }
                                    //builder.setPositiveButton("确定", selectedWxIdIndex.size()>0?onOkClickListener:null);
                                    Button button = selectDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    if(button!=null)
                                    {
                                        button.setEnabled(selectedWxIdIndex.size()>0);
                                    }
                                }
                            };
                            builder.setMultiChoiceItems(friends, checks, onMultiChoiceClickListener);

                            builder.setPositiveButton("确定", onOkClickListener);
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            //builder.show();
                            selectDialog = builder.create();
                            selectDialog.show();
                            listViewSelectDialog = selectDialog.getListView();

                            Button button = selectDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            if(button!=null)
                            {
                                button.setEnabled(false);
                            }

                        } else {
                            text = "无联系人";
                        }
                    }


                } catch (Exception e) {
                    text = "解析结果失败>>"+e.getMessage();
                    Log.e(LOG_TAG, "jsonerr", e);
                }
                if (text.length() > 0) {
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }


            }

        };
        labelMasterSpeakers.setOnClickListener(onSelectWxIdClickListener);
        labelSlaveChatroomIds.setOnClickListener(onSelectWxIdClickListener);

    }

    private boolean initSDK()
    {
        boolean bok = false;
        if(editAppId.getText().toString().equals(""))
        {
            Toast.makeText(mContext, "AppId不能为空！", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editAuthCode.getText().toString().equals(""))
        {
            Toast.makeText(mContext, "授权码不能为空不能为空！", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isInited && !editAppId.getText().toString().equals("") && !editAuthCode.getText().toString().equals("")) {
            //初始化
            parseResult(wToolSDK.init(editAppId.getText().toString(),editAuthCode.getText().toString()));
            configUtils.save(ConfigUtils.KEY_APPID, editAppId.getText().toString());
            configUtils.save(ConfigUtils.KEY_AUTHCODE, editAuthCode.getText().toString());
            isInited = true;
            bok = true;
        }
        return isInited;
    }

    private void parseResult(String result) {
        String text = "";
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.getInt("result") == 0) {
                text = "操作成功";
            } else {
                text = jsonObject.getString("errmsg");
            }
        } catch (Exception e) {
            text = "解析结果失败";
        }
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
    }


}
