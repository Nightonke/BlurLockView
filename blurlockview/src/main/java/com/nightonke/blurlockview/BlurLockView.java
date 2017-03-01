package com.nightonke.blurlockview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Directions.ShowType;
import com.nightonke.blurlockview.Eases.EaseType;

import java.util.Stack;

/**
 * Created by Weiping on 2016/3/16.
 */
public class BlurLockView extends FrameLayout
        implements
        BigButtonView.OnPressListener,
        SmallButtonView.OnPressListener {

    private final char CHARS[][] = {
            {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'},
            {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'},
            {   'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'  },
            {        'Z', 'X', 'C', 'V', 'B', 'N', 'M'       }
    };

    private Password type = Password.NUMBER;

    private int passwordLength = 4;
    private String correctPassword = null;
    private int incorrectInputTimes = 0;
    private Typeface typeface;

    private boolean animationIsPlaying = false;

    private Stack<String> passwordStack = null;

    private TextView title;
    private Indicator indicator;

    private BigButtonView[] bigButtonViews;
    private SmallButtonView[][] smallButtonViews;
    private BlurView mBlurView;
    private TextView leftButton;
    private TextView rightButton;

    private OnLeftButtonClickListener onLeftButtonClickListener;
    private OnPasswordInputListener onPasswordInputListener;

    public BlurLockView(Context context) {
        this(context, null);
    }

    public BlurLockView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    /**
     * Init.
     */
    private void init() {
        if (isInEditMode()) {
          return;
        }
        // number password
        LayoutInflater.from(getContext()).inflate(R.layout.number_blur_lock_view, this, true);

        bigButtonViews = new BigButtonView[10];
        bigButtonViews[0] = (BigButtonView)findViewById(R.id.button_0);
        bigButtonViews[1] = (BigButtonView)findViewById(R.id.button_1);
        bigButtonViews[2] = (BigButtonView)findViewById(R.id.button_2);
        bigButtonViews[3] = (BigButtonView)findViewById(R.id.button_3);
        bigButtonViews[4] = (BigButtonView)findViewById(R.id.button_4);
        bigButtonViews[5] = (BigButtonView)findViewById(R.id.button_5);
        bigButtonViews[6] = (BigButtonView)findViewById(R.id.button_6);
        bigButtonViews[7] = (BigButtonView)findViewById(R.id.button_7);
        bigButtonViews[8] = (BigButtonView)findViewById(R.id.button_8);
        bigButtonViews[9] = (BigButtonView)findViewById(R.id.button_9);

        String[] texts = getResources().getStringArray(R.array.default_big_button_text);
        String[] subTexts = getResources().getStringArray(R.array.default_big_button_sub_text);
        for (int i = 0; i < 10; i++) {
            bigButtonViews[i].setOnPressListener(this);
            bigButtonViews[i].setText(texts[i]);
            bigButtonViews[i].setSubText(subTexts[i]);
        }

        bigButtonViews[0].setSubTextVisibility(View.GONE);
        bigButtonViews[1].setSubTextVisibility(View.INVISIBLE);

        // text password
        smallButtonViews = new SmallButtonView[4][10];

        // get screen width
        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int buttonHorizontalMargin = 6;
        int buttonVerticalMargin = 24;
        int buttonWidth = (width - 11 * buttonHorizontalMargin) / 10;

        // add buttons to lines
        LinearLayout line1 = (LinearLayout)findViewById(R.id.line_1);
        for (int i = 0; i < 10; i++) {
            smallButtonViews[0][i] = new SmallButtonView(getContext());
            smallButtonViews[0][i].setOnPressListener(this);
            smallButtonViews[0][i].setText(CHARS[0][i] + "");
            smallButtonViews[0][i].setWidth(buttonWidth);
            smallButtonViews[0][i].setHeight(buttonWidth);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    buttonWidth,
                    buttonWidth
            );
            if (i == 0)
                params.setMargins(buttonHorizontalMargin, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            else if (i == 9)
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin, buttonVerticalMargin / 2);
            else
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            line1.addView(smallButtonViews[0][i], params);
        }

        LinearLayout line2 = (LinearLayout)findViewById(R.id.line_2);
        for (int i = 0; i < 10; i++) {
            smallButtonViews[1][i] = new SmallButtonView(getContext());
            smallButtonViews[1][i].setOnPressListener(this);
            smallButtonViews[1][i].setText(CHARS[1][i] + "");
            smallButtonViews[1][i].setWidth(buttonWidth);
            smallButtonViews[1][i].setHeight(buttonWidth);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    buttonWidth,
                    buttonWidth
            );
            if (i == 0)
                params.setMargins(buttonHorizontalMargin, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            else if (i == 9)
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin, buttonVerticalMargin / 2);
            else
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            line2.addView(smallButtonViews[1][i], params);
        }

        LinearLayout line3 = (LinearLayout)findViewById(R.id.line_3);
        for (int i = 0; i < 9; i++) {
            smallButtonViews[2][i] = new SmallButtonView(getContext());
            smallButtonViews[2][i].setOnPressListener(this);
            smallButtonViews[2][i].setText(CHARS[2][i] + "");
            smallButtonViews[2][i].setWidth(buttonWidth);
            smallButtonViews[2][i].setHeight(buttonWidth);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    buttonWidth,
                    buttonWidth
            );

            if (i == 0)
                params.setMargins(buttonHorizontalMargin, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            else if (i == 8)
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin, buttonVerticalMargin / 2);
            else
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            line3.addView(smallButtonViews[2][i], params);
        }

        LinearLayout line4 = (LinearLayout)findViewById(R.id.line_4);
        for (int i = 0; i < 7; i++) {
            smallButtonViews[3][i] = new SmallButtonView(getContext());
            smallButtonViews[3][i].setOnPressListener(this);
            smallButtonViews[3][i].setText(CHARS[3][i] + "");
            smallButtonViews[3][i].setWidth(buttonWidth);
            smallButtonViews[3][i].setHeight(buttonWidth);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    buttonWidth,
                    buttonWidth
            );

            if (i == 0)
                params.setMargins(buttonHorizontalMargin, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            else if (i == 6)
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin, buttonVerticalMargin / 2);
            else
                params.setMargins(buttonHorizontalMargin / 2, buttonVerticalMargin / 2, buttonHorizontalMargin / 2, buttonVerticalMargin / 2);
            line4.addView(smallButtonViews[3][i], params);
        }

        passwordStack = new Stack<>();

        mBlurView = (BlurView)findViewById(R.id.blurview);
        mBlurView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Resources resources = getResources();

        indicator = (Indicator)findViewById(R.id.indicator);
        indicator.setPasswordLength(passwordLength);

        title = (TextView)findViewById(R.id.title);
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.default_title_text_color));
        title.setTextSize(resources.getInteger(R.integer.default_title_text_size));

        leftButton = (TextView)findViewById(R.id.left_button);
        leftButton.setTextColor(ContextCompat.getColor(getContext(), R.color.default_left_button_text_color));
        leftButton.setTextSize(resources.getInteger(R.integer.default_left_button_text_size));
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLeftButtonClickListener != null) onLeftButtonClickListener.onClick();
            }
        });

        rightButton = (TextView)findViewById(R.id.right_button);
        rightButton.setTextColor(ContextCompat.getColor(getContext(), R.color.default_right_button_text_color));
        rightButton.setTextSize(resources.getInteger(R.integer.default_right_button_text_size));
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordStack.size() > 0) {
                    passwordStack.pop();
                    indicator.delete();
                }
            }
        });
        setType(type, false);
    }

    /**
     * Show the text keyboard smoothly or not.
     *
     * @param smoothly Smoothly or not.
     */
    private void showText(boolean smoothly) {
        if (animationIsPlaying) return;
        animationIsPlaying = true;
        if (smoothly) {
            ObjectAnimator.ofFloat(findViewById(R.id.layout_123), "alpha", 1f, 0f)
                    .setDuration(500).start();
            ObjectAnimator.ofFloat(findViewById(R.id.layout_456), "alpha", 1f, 0f)
                    .setDuration(500).start();
            ObjectAnimator.ofFloat(findViewById(R.id.layout_789), "alpha", 1f, 0f)
                    .setDuration(500).start();
            ObjectAnimator.ofFloat(findViewById(R.id.button_0), "alpha", 1f, 0f)
                    .setDuration(500).start();
            ObjectAnimator showAnimator =
                    ObjectAnimator.ofFloat(findViewById(R.id.text_layout), "alpha", 0f, 1f);
            showAnimator.setDuration(500).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    findViewById(R.id.text_layout).setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.layout_123).setVisibility(INVISIBLE);
                    findViewById(R.id.layout_456).setVisibility(INVISIBLE);
                    findViewById(R.id.layout_789).setVisibility(INVISIBLE);
                    findViewById(R.id.button_0).setVisibility(INVISIBLE);
                    animationIsPlaying = false;
                }
            });
            showAnimator.start();
        } else {
            findViewById(R.id.layout_123).setVisibility(INVISIBLE);
            findViewById(R.id.layout_456).setVisibility(INVISIBLE);
            findViewById(R.id.layout_789).setVisibility(INVISIBLE);
            findViewById(R.id.button_0).setVisibility(INVISIBLE);
            findViewById(R.id.text_layout).setVisibility(VISIBLE);
            animationIsPlaying = false;
        }
    }

    /**
     * Show the number keyboard smoothly or not.
     *
     * @param smoothly Smoothly or not.
     */
    private void showNumber(boolean smoothly) {
        if (animationIsPlaying) return;
        animationIsPlaying = true;
        if (smoothly) {
            ObjectAnimator.ofFloat(findViewById(R.id.layout_123), "alpha", 0f, 1f)
                    .setDuration(500).start();
            ObjectAnimator.ofFloat(findViewById(R.id.layout_456), "alpha", 0f, 1f)
                    .setDuration(500).start();
            ObjectAnimator.ofFloat(findViewById(R.id.layout_789), "alpha", 0f, 1f)
                    .setDuration(500).start();
            ObjectAnimator.ofFloat(findViewById(R.id.button_0), "alpha", 0f, 1f)
                    .setDuration(500).start();
            ObjectAnimator showAnimator =
                    ObjectAnimator.ofFloat(findViewById(R.id.text_layout), "alpha", 1f, 0f);
            showAnimator.setDuration(500).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    findViewById(R.id.layout_123).setVisibility(VISIBLE);
                    findViewById(R.id.layout_456).setVisibility(VISIBLE);
                    findViewById(R.id.layout_789).setVisibility(VISIBLE);
                    findViewById(R.id.button_0).setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.text_layout).setVisibility(INVISIBLE);
                    animationIsPlaying = false;
                }
            });
            showAnimator.start();
        } else {
            findViewById(R.id.layout_123).setVisibility(VISIBLE);
            findViewById(R.id.layout_456).setVisibility(VISIBLE);
            findViewById(R.id.layout_789).setVisibility(VISIBLE);
            findViewById(R.id.button_0).setVisibility(VISIBLE);
            findViewById(R.id.text_layout).setVisibility(INVISIBLE);
            animationIsPlaying = false;
        }
    }

    /**
     * Set the view that need to be blurred.
     *
     * @param blurredView The view.
     */
    public void setBlurredView(View blurredView) {
        mBlurView.setBlurredView(blurredView);
    }

    /**
     * Set the listener.
     *
     * @param onLeftButtonClickListener Listener.
     */
    public void setOnLeftButtonClickListener(OnLeftButtonClickListener onLeftButtonClickListener) {
        this.onLeftButtonClickListener = onLeftButtonClickListener;
    }

    /**
     * Set the listener.
     *
     * @param onPasswordInputListener Listener.
     */
    public void setOnPasswordInputListener(OnPasswordInputListener onPasswordInputListener) {
        this.onPasswordInputListener = onPasswordInputListener;
    }

    /**
     * From the button views.
     *
     * @param string The string from button views.
     */
    @Override
    public void onPress(String string) {
        if (correctPassword == null) {
            throw new RuntimeException("The correct password has NOT been set!");
        }
        if (passwordStack.size() >= passwordLength) return;
        passwordStack.push(string);
        indicator.add();
        StringBuilder nowPassword = new StringBuilder("");
        for (String s : passwordStack) {
            nowPassword.append(s);
        }
        String nowPasswordString = nowPassword.toString();
        if (correctPassword.equals(nowPasswordString)) {
            // correct password
            if (onPasswordInputListener != null)
                onPasswordInputListener.correct(nowPasswordString);
        } else {
            if (correctPassword.length() > nowPasswordString.length()) {
                // input right now
                if (onPasswordInputListener != null)
                    onPasswordInputListener.input(nowPasswordString);
            } else {
                // incorrect password
                if (onPasswordInputListener != null)
                    onPasswordInputListener.incorrect(nowPasswordString);
                // perform the clear animation
                incorrectInputTimes++;
                indicator.clear();
                passwordStack.clear();
            }
        }
    }

    /**
     * Prevent click 2 or above buttons at the same time.
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            if (Password.NUMBER.equals(type)) {
                for (int i = 0; i < bigButtonViews.length; i++) bigButtonViews[i].clearAnimation();
            } else if (Password.TEXT.equals(type)) {
                for (int i = 0; i < smallButtonViews.length; i++) {
                    for (int j = 0; j < smallButtonViews[i].length; j++) {
                        if (smallButtonViews[i][j] != null) smallButtonViews[i][j].clearAnimation();
                    }
                }
            }
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Set big buttons' background.
     *
     * @param id
     */
    public void setBigButtonViewsBackground(int id) {
        for (int i = 0; i < 10; i++) bigButtonViews[i].setBackground(id);
    }

    /**
     * Set big buttons' click effect.
     *
     * @param id
     */
    public void setBigButtonViewsClickEffect(int id) {
        for (int i = 0; i < 10; i++) bigButtonViews[i].setEffect(id);
    }

    /**
     * Set the click effect duration.
     *
     * @param duration
     */
    public void setBigButtonViewsClickEffectDuration(int duration) {
        for (int i = 0; i < 10; i++) bigButtonViews[i].setEffectDuration(duration);
    }

    /**
     * Set small buttons' background.
     *
     * @param id
     */
    public void setSmallButtonViewsBackground(int id) {
        for (int i = 0; i < smallButtonViews.length; i++)
            for (int j = 0; j < smallButtonViews[i].length; j++)
                if (smallButtonViews[i][j] != null)
                    smallButtonViews[i][j].setBackground(id);
    }

    /**
     * Set small buttons' click effect.
     *
     * @param id
     */
    public void setSmallButtonViewsClickEffect(int id) {
        for (int i = 0; i < smallButtonViews.length; i++)
            for (int j = 0; j < smallButtonViews[i].length; j++)
                if (smallButtonViews[i][j] != null)
                    smallButtonViews[i][j].setEffect(id);
    }

    /**
     * Set the click effect duration.
     *
     * @param duration
     */
    public void setSmallButtonViewsClickEffectDuration(int duration) {
        for (int i = 0; i < smallButtonViews.length; i++)
            for (int j = 0; j < smallButtonViews[i].length; j++)
                if (smallButtonViews[i][j] != null)
                    smallButtonViews[i][j].setEffectDuration(duration);
    }
    
    /**
     * Set all the fonts.
     *
     * @param typeface
     */
    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
        if (type.equals(Password.NUMBER)) {
            for (int i = 0; i < 10; i++) bigButtonViews[i].setTypeFace(typeface);
        } else if (type.equals(Password.TEXT)) {
            for (int i = 0; i < smallButtonViews.length; i++)
                for (int j = 0; j < smallButtonViews[i].length; j++)
                    if (smallButtonViews[i][j] != null)
                        smallButtonViews[i][j].setTypeFace(typeface);
        }
        title.setTypeface(typeface);
        leftButton.setTypeface(typeface);
        rightButton.setTypeface(typeface);
    }

    /**
     * Set all the text color.
     *
     * @param color
     */
    public void setTextColor(int color) {
        if (type.equals(Password.NUMBER)) {
            for (int i = 0; i < 10; i++) {
                bigButtonViews[i].setTextColor(color);
                bigButtonViews[i].setSubTextColor(color);
            }
        } else if (type.equals(Password.TEXT)) {
            for (int i = 0; i < smallButtonViews.length; i++)
                for (int j = 0; j < smallButtonViews[i].length; j++)
                    if (smallButtonViews[i][j] != null)
                        smallButtonViews[i][j].setTextColor(color);
        }
        title.setTextColor(color);
        leftButton.setTextColor(color);
        rightButton.setTextColor(color);
    }

    /**
     * Set the length of the password.
     * Default length is 4.
     *
     * @param passwordLength
     */
    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
        indicator.setPasswordLength(passwordLength);
        passwordStack.clear();
        correctPassword = null;
    }

    /**
     * Get the password type.
     *
     * @return
     */
    public Password getType() {
        return type;
    }

    /**
     * Set the password type.
     *
     * @param type Number or text.
     */
    public void setType(Password type, boolean smoothly) {
        if (animationIsPlaying) return;
        this.type = type;
        indicator.clear();
        passwordStack.clear();
        if (Password.NUMBER.equals(type)) {
            showNumber(smoothly);
        } else if (Password.TEXT.equals(type)) {
            showText(smoothly);
        }
    }

    /**
     * Set the title text.
     *
     * @param string
     */
    public void setTitle(String string) {
        title.setText(string);
    }

    /**
     * Set the text of left button.
     *
     * @param string
     */
    public void setLeftButton(String string) {
        leftButton.setText(string);
    }

    /**
     * Set the text of right button.
     *
     * @param string
     */
    public void setRightButton(String string) {
        rightButton.setText(string);
    }

    /**
     * Set the target password.
     *
     * @param correctPassword The target password.
     */
    public void setCorrectPassword(String correctPassword) {
        setPasswordLength(correctPassword.length());
        this.correctPassword = correctPassword;
    }

    /**
     * You can use this to reset the incorrect input times.
     *
     * @param incorrectInputTimes The incorrect input times.
     */
    public void setIncorrectInputTimes(int incorrectInputTimes) {
        this.incorrectInputTimes = incorrectInputTimes;
    }

    /**
     * Return the incorrect input times.
     *
     * @return Incorrect input times.
     */
    public int getIncorrectInputTimes() {
        return incorrectInputTimes;
    }

    /**
     * Invalidate the blur view.
     */
    public void update() {
        mBlurView.invalidate();
    }

    /**
     * Show this BlurLockView.
     *
     * @param duration Duration, in ms.
     * @param showType Direction, in ShowType.
     * @param easeType Ease type, in EaseType.
     */
    public void show(int duration, ShowType showType, EaseType easeType) {
        if (animationIsPlaying) return;
        animationIsPlaying = true;
        indicator.clear();
        passwordStack.clear();
        ObjectAnimator animator = null;
        setVisibility(VISIBLE);
        if (showType.equals(ShowType.FROM_TOP_TO_BOTTOM)) {
            animator = ObjectAnimator.ofFloat(this, "translationY",
                    getTranslationY() - getHeight(),
                    getTranslationY());
        } else if (showType.equals(ShowType.FROM_RIGHT_TO_LEFT)) {
            animator = ObjectAnimator.ofFloat(this, "translationX",
                    getTranslationX() + getWidth(),
                    getTranslationX());
        } else if (showType.equals(ShowType.FROM_BOTTOM_TO_TOP)) {
            animator = ObjectAnimator.ofFloat(this, "translationY",
                    getTranslationY() + getHeight(),
                    getTranslationY());
        } else if (showType.equals(ShowType.FROM_LEFT_TO_RIGHT)) {
            animator = ObjectAnimator.ofFloat(this, "translationX",
                    getTranslationX() - getWidth(),
                    getTranslationX());
        } else if (showType.equals(ShowType.FADE_IN)) {
            animator = ObjectAnimator.ofFloat(this, "alpha",
                    0,
                    1);
        }
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                update();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animationIsPlaying = false;
            }
        });
        animator.setInterpolator(InterpolatorFactory.getInterpolator(easeType));
        animator.start();
    }

    /**
     * Hide this BlurLockView.
     *
     * @param duration Duration, in ms.
     * @param hideType Direction, in HideType.
     * @param easeType Ease type, in EaseType.
     */
    public void hide(int duration, HideType hideType, EaseType easeType) {
        if (animationIsPlaying) return;
        animationIsPlaying = true;
        ObjectAnimator animator = null;
        final float originalX = getTranslationX();
        final float originalY = getTranslationY();
        if (hideType.equals(HideType.FROM_TOP_TO_BOTTOM)) {
            animator = ObjectAnimator.ofFloat(this, "translationY",
                    getTranslationY(),
                    getTranslationY() + getHeight());
        } else if (hideType.equals(HideType.FROM_RIGHT_TO_LEFT)) {
            animator = ObjectAnimator.ofFloat(this, "translationX",
                    getTranslationX(),
                    getTranslationX() - getWidth());
        } else if (hideType.equals(HideType.FROM_BOTTOM_TO_TOP)) {
            animator = ObjectAnimator.ofFloat(this, "translationY",
                    getTranslationY(),
                    getTranslationY() - getHeight());
        } else if (hideType.equals(HideType.FROM_LEFT_TO_RIGHT)) {
            animator = ObjectAnimator.ofFloat(this, "translationX",
                    getTranslationX(),
                    getTranslationX() + getWidth());
        } else if (hideType.equals(HideType.FADE_OUT)) {
            animator = ObjectAnimator.ofFloat(this, "alpha",
                    1,
                    0);
        }
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                update();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(INVISIBLE);
                setTranslationX(originalX);
                setTranslationY(originalY);
                setAlpha(1);
                animationIsPlaying = false;
            }
        });
        animator.setInterpolator(InterpolatorFactory.getInterpolator(easeType));
        animator.start();
    }

    public interface OnPasswordInputListener {
        void correct(String inputPassword);
        void incorrect(String inputPassword);
        void input(String inputPassword);
    }

    public interface OnLeftButtonClickListener {
        void onClick();
    }

    /**
     * Get the title.
     * @return
     */
    public TextView getTitle() {
        return title;
    }

    /**
     * Get the left button.
     * @return
     */
    public TextView getLeftButton() {
        return leftButton;
    }

    /**
     * Get the right button.
     * @return
     */
    public TextView getRightButton() {
        return rightButton;
    }

    /**
     * Get the numbers.
     * @return
     */
    public BigButtonView[] getBigButtonViews() {
        return bigButtonViews;
    }

    /**
     * Get the texts.
     * @return
     */
    public SmallButtonView[][] getSmallButtonViews() {
        return smallButtonViews;
    }

    /**
     * Set the blur radius.
     */
    public void setBlurRadius(int blurRadius) {
        mBlurView.setBlurRadius(blurRadius);
        update();
    }

    /**
     * Get the blur radius.
     * @return
     */
    public int getBlurRadius() {
        return mBlurView.getBlurRadius();
    }

    /**
     * Set the downsample factor.
     * @param downsampleFactor
     */
    public void setDownsampleFactor(int downsampleFactor) {
        mBlurView.setDownsampleFactor(downsampleFactor);
        update();
    }

    /**
     * Get the downsample factor.
     * @return
     */
    public int getDownsampleFactor() {
        return mBlurView.getDownsampleFactor();
    }

    /**
     * Set the overlay color.
     * @param color
     */
    public void setOverlayColor(int color) {
        mBlurView.setOverlayColor(color);
        update();
    }

    /**
     * Get the overlay color.
     * @return
     */
    public int getOverlayColor() {
        return mBlurView.getmOverlayColor();
    }
}
