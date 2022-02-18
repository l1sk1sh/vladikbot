package com.l1sk1sh.vladikbot.services.mood;

import com.l1sk1sh.vladikbot.models.mood.MoodPoint;
import com.l1sk1sh.vladikbot.models.mood.MoodStatus;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Random;

class MoodFieldConst {

    public static final MoodPoint SADNESS_CENTER_BORDER = new MoodPoint(-10, 0, MoodStatus.SADNESS);
    public static final MoodPoint WRAITH_CENTRE_BORDER  = new MoodPoint(-10, 10, MoodStatus.WRAITH);
    public static final MoodPoint CURIOSITY_CENTRE_BORDER   = new MoodPoint(0, 8, MoodStatus.CURIOSITY);
    public static final MoodPoint PASSION_CENTRE_BORDER = new MoodPoint(8, 8, MoodStatus.PASSION);
    public static final MoodPoint HAPPINESS_CENTRE_BORDER   = new MoodPoint(8, 0, MoodStatus.HAPPINESS);
    public static final MoodPoint SERENITY_CENTRE_BORDER    = new MoodPoint(8, -8, MoodStatus.SERENITY);
    public static final MoodPoint CAUTION_CENTRE_BORDER = new MoodPoint(0, -8, MoodStatus.CAUTION);
    public static final MoodPoint ANNOYANCE_CENTRE_BORDER   = new MoodPoint(-8, -8, MoodStatus.ANNOYANCE);
    public static final MoodPoint WONDER_CENTRE         = new MoodPoint(-3, -3, MoodStatus.WONDER);
    public static final MoodPoint ANGER_CENTRE          = new MoodPoint(-3, 3, MoodStatus.ANGER);
    public static final MoodPoint ENTHUSIASTIC_CENTRE   = new MoodPoint(3, 3, MoodStatus.ENTHUSIASTIC);
    public static final MoodPoint CALM_CENTRE           = new MoodPoint(3, -3, MoodStatus.CALM);

    public static final List<MoodPoint> MOOD_LIST =
            List.of(SADNESS_CENTER_BORDER, WRAITH_CENTRE_BORDER, CURIOSITY_CENTRE_BORDER,
                    PASSION_CENTRE_BORDER, HAPPINESS_CENTRE_BORDER, SERENITY_CENTRE_BORDER,
                    CAUTION_CENTRE_BORDER, ANNOYANCE_CENTRE_BORDER, WONDER_CENTRE,
                    ANGER_CENTRE, ENTHUSIASTIC_CENTRE, CALM_CENTRE);

}

public class MoodManager {

    private MoodPoint moodPoint = new MoodPoint(0, 0, MoodStatus.NEUTRAL);
    private int maxAxisValue = 10, minAxisValue = -10;
    private Random random = new Random();
    /**
     * Updates MoodStatus with position in MoodMatrix coordinates
     */
    private void updateMoodStatus() {
        if(moodPoint.getX() == 0 && moodPoint.getY() == 0) {
            moodPoint.setMood(MoodStatus.NEUTRAL);
        } else if(moodPoint.getX() < -5 && moodPoint.getY() > -5
                && moodPoint.getY() < 5) {
            moodPoint.setMood(MoodStatus.SADNESS);
        } else if(moodPoint.getX() < -5 && moodPoint.getY() > 5) {
            moodPoint.setMood(MoodStatus.WRAITH);
        } else if(moodPoint.getX() > -5 && moodPoint.getX() < 5
                && moodPoint.getY() > 5) {
            moodPoint.setMood(MoodStatus.CURIOSITY);
        } else if(moodPoint.getX() > 5 && moodPoint.getY() > 5) {
            moodPoint.setMood(MoodStatus.PASSION);
        } else if(moodPoint.getX() > 5 && moodPoint.getY() > -5
                && moodPoint.getY() < 5) {
            moodPoint.setMood(MoodStatus.HAPPINESS);
        } else if(moodPoint.getX() > 5 && moodPoint.getY() < -5) {
            moodPoint.setMood(MoodStatus.SERENITY);
        } else if(moodPoint.getX() < 5 && moodPoint.getX() > -5
                && moodPoint.getY() < 5) {
            moodPoint.setMood(MoodStatus.CAUTION);
        } else if(moodPoint.getX() < -5 && moodPoint.getY() < -5) {
            moodPoint.setMood(MoodStatus.ANNOYANCE);
        } else if(moodPoint.getX() >= -5 && moodPoint.getX() <= 0
                && moodPoint.getY() <= 0 && moodPoint.getY() >= -5) {
            moodPoint.setMood(MoodStatus.WONDER);
        } else if(moodPoint.getX() >= 0 && moodPoint.getX() <= 5
                && moodPoint.getY() >= -5 && moodPoint.getY() <= 0) {
            moodPoint.setMood(MoodStatus.CALM);
        } else if(moodPoint.getX() >= 0 && moodPoint.getX() <= 5
                && moodPoint.getY() >= 0 && moodPoint.getY() <= 5) {
            moodPoint.setMood(MoodStatus.ENTHUSIASTIC);
        } else if(moodPoint.getX() >= -5 && moodPoint.getX() <= 0
                && moodPoint.getY() >= 0 && moodPoint.getY() <= 5) {
            moodPoint.setMood(MoodStatus.ANGER);
        }
    }

    /**
     * Makes step in mood matrix
     */
    private void makeMoodStep(MoodStatus moodStatus) {

        MoodPoint destination = MoodFieldConst.MOOD_LIST
                .stream()
                .filter(constant -> constant.getMood().equals(moodStatus))
                .findAny()
                .get();

        // extra chance to make additional step
        int randomSaltX = random.nextInt(22) / 10;
        int randomSaltY = random.nextInt(22) / 10;

        int stepX = 1 + randomSaltX;
        int stepY = 1 + randomSaltY;

        if(destination.getX() > moodPoint.getX()) {
            moodPoint.setX(moodPoint.getX() + stepX);
        } else if(destination.getX() < moodPoint.getX()) {
            moodPoint.setX(moodPoint.getX() - stepX);
        }

        if(destination.getY() > moodPoint.getY()) {
            moodPoint.setY(moodPoint.getY() + stepY);
        } else if(destination.getY() < moodPoint.getY()) {
            moodPoint.setY(moodPoint.getY() - stepY);
        }

    }

    /**
     * Resets Vladik mood if he's brain overloads
     * @return last MoodStatus before brain overloads
     */
    private MoodStatus brainOverload() {
        MoodStatus lastMood = moodPoint.getMood();

        moodPoint.setMood(MoodStatus.NEUTRAL);
        moodPoint.setX(0);
        moodPoint.setY(0);

        return lastMood;
    }

    private boolean isBrainOverloaded() {
        return moodPoint.getX() > maxAxisValue
                || moodPoint.getX() < minAxisValue
                || moodPoint.getY() > maxAxisValue
                || moodPoint.getY() < minAxisValue;
    }

    /**
     * Some magic is going here
     *
     * @param message argument for incoming updates....
     * @param mood is TAG that is responsible for the emotional background of message
     * @return current Vladik Mood
     */
    public MoodStatus changeMood(Message message, MoodStatus mood) {

        if(isBrainOverloaded()) {
            return brainOverload();
        }

        makeMoodStep(mood);
        updateMoodStatus();

        return moodPoint.getMood();
    }

}

