package com.littlejie.password;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.littlejie.core.base.BaseActivity;
import com.littlejie.core.manager.TintManager;
import com.littlejie.password.storage.PasswordStorage;
import com.littlejie.password.ui.SetPwdView;

public class PwdSettingActivity extends BaseActivity {

    private static final int MAX_RETRY_TIMES = 5;
    private Toolbar toolbar;
    private SetPwdView setPwdView;
    private ImageView ivLike;

    private int type = Constants.TYPE_SET_PASSWORD;

    private int pwdRetryTime;
    //密码
    private String password;

    @Override
    protected int getPageLayoutID() {
        return R.layout.activity_pwd_setting;
    }

    @Override
    protected void initData() {
        if (getIntent() != null) {
            type = getIntent().getIntExtra(Constants.PARAMS_SET_PASSWORD_TYPE,
                    Constants.TYPE_SET_PASSWORD);
        }

        if (!TextUtils.isEmpty(PasswordStorage.get(this))) {
            type = Constants.TYPE_CLOSE_PASSWORD;
        }
    }

    @Override
    protected void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setPwdView = (SetPwdView) findViewById(R.id.set_password);
        ivLike = (ImageView) findViewById(R.id.iv_like);
        setTitle();
    }

    private void setTitle() {
        switch (type) {
            case Constants.TYPE_SET_PASSWORD:
            case Constants.TYPE_REENTER_PASSWORD:
                toolbar.setTitle(R.string.set_passsword);
                break;
            case Constants.TYPE_CLOSE_PASSWORD:
                toolbar.setTitle(R.string.turn_off_password);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initViewListener() {
        setPwdView.setOnFinishListener(new OnFinishListener() {
            @Override
            public void onFinish(String pwd) {
                processPasswordInput(pwd);
            }
        });
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                ivLike.setImageDrawable(TintManager.tintDrawable(PwdSettingActivity.this,
                        R.drawable.icon_small_like_it_normal, getResources().getColor(R.color.main_color)));
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("测试")
                .setCancelable(false)
                .setMessage("测试自定义Dialog Style")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    protected void process() {

    }

    private void processPasswordInput(String pwd) {
        if (type == Constants.TYPE_SET_PASSWORD) {
            password = pwd;
            type = Constants.TYPE_REENTER_PASSWORD;
            setPwdView.setTitle(R.string.reenter_password);
            setPwdView.resetPassword();
        } else if (type == Constants.TYPE_REENTER_PASSWORD) {
            if (pwd.equals(password)) {
                PasswordStorage.save(this, password);
                finish();
            } else {
                setPwdView.setTip(R.string.password_not_match_enter_again);
                setPwdView.resetPassword();
            }
        } else if (type == Constants.TYPE_CLOSE_PASSWORD) {
            if (PasswordStorage.encryptPassword(this, pwd).equals(PasswordStorage.get(this))) {
                PasswordStorage.clear(this);
                finish();
            } else {
                if (pwdRetryTime == MAX_RETRY_TIMES) {
                    PasswordStorage.clear(this);
                    finish();
                    return;
                }
                pwdRetryTime += 1;
                setPwdView.resetPassword();
                setPwdView.setTip(getString(R.string.more_attempts, MAX_RETRY_TIMES - pwdRetryTime));
                setPwdView.setDesc(R.string.input_password_too_many_times_and_will_relogin);
            }
        }
        setTitle();
    }
}
