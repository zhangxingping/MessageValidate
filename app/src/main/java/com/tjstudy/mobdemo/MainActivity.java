package com.tjstudy.mobdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.socks.library.KLog;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Mob 短信验证 测试demo
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String APPKEY = "1705b836da0a0";
    private static final String APPSECRECT = "356b3f47228c2d31fc97b07f0b3fabd5";
    private EditText mEtPhone;
    private EditText mEtPhoneCode;
    private Button mBtnSubmitUser;
    private TextView mTvGetPhoneCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSMSSDK();

        initView();
        setListener();
    }


    private void initView() {
        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mEtPhoneCode = (EditText) findViewById(R.id.et_phone_code);
        mTvGetPhoneCode = (TextView) findViewById(R.id.tv_get_phone_code);
        mBtnSubmitUser = (Button) findViewById(R.id.btn_submit_user);
    }

    private void setListener() {
        mTvGetPhoneCode.setOnClickListener(this);
        mBtnSubmitUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击获取验证码控件
            case R.id.tv_get_phone_code:
                mTvGetPhoneCode.requestFocus();
                if (validatePhone()) {
                    //启动获取验证码 86是中国
                    String phone = mEtPhone.getText().toString().trim();
                    SMSSDK.getVerificationCode("86", phone);//发送短信验证码到手机号
                    timer.start();//使用计时器 设置验证码的时间限制
                }
                break;
            //点击提交信息按钮
            case R.id.btn_submit_user:
                submitInfo();
                break;
        }
    }

    /**
     * 验证用户的其他信息
     * 这里验证两次密码是否一致 以及验证码判断
     */
    private void submitInfo() {
        //密码验证
        KLog.e("提交按钮点击了");
        String phone = mEtPhone.getText().toString().trim();
        String code = mEtPhoneCode.getText().toString().trim();
        SMSSDK.submitVerificationCode("86", phone, code);//提交验证码  在eventHandler里面查看验证结果
    }

    /**
     * 使用计时器来限定验证码
     * 在发送验证码的过程 不可以再次申请获取验证码 在指定时间之后没有获取到验证码才能重新进行发送
     * 这里限定的时间是60s
     */
    private CountDownTimer timer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mTvGetPhoneCode.setText((millisUntilFinished / 1000) + "秒后可重发");
        }

        @Override
        public void onFinish() {
            mTvGetPhoneCode.setEnabled(true);
            mTvGetPhoneCode.setText("获取验证码");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //防止使用短信验证 产生内存溢出问题
        SMSSDK.unregisterAllEventHandler();
    }

    /**
     * 验证手机号码是否符合要求，11位 并且没有注册过
     *
     * @return 是否符合要求
     */
    private boolean validatePhone() {
        String phone = mEtPhone.getText().toString().trim();
        return true;
    }
    private void initSMSSDK() {
        //初始化短信验证
        SMSSDK.initSDK(this, APPKEY, APPSECRECT);

        //注册短信回调
        SMSSDK.registerEventHandler(new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                switch (event) {
                    case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE:
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            KLog.e("验证成功");
                        } else {
                            KLog.e("验证失败");
                        }
                        break;
                    case SMSSDK.EVENT_GET_VERIFICATION_CODE:
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            KLog.e("获取验证成功");
                        } else {
                            KLog.e("获取验证失败");
                        }
                        break;
                }
            }
        });
    }
}