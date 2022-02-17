package com.l1sk1sh.vladikbot.services.mood;

import com.l1sk1sh.vladikbot.models.mood.MoodPoint;
import com.l1sk1sh.vladikbot.models.mood.MoodStatus;

import javax.inject.Singleton;

@Singleton
public class MoodManager {

    private MoodPoint moodPoint = new MoodPoint(0, 0, MoodStatus.NEUTRAL);
    private int maxAxisValue = 10, minAxisValue = -10;

    // hardcode go brrrr
    private void updateMoodStatus() {
        if(moodPoint.getX() == 0 && moodPoint.getY() == 0) {
            moodPoint.setMood(MoodStatus.NEUTRAL);
        } else if(moodPoint.getX() < -5 && moodPoint.getY() > -5 && moodPoint.getY() < 5) {
            moodPoint.setMood(MoodStatus.SADNESS);
        } else if(moodPoint.getX() < -5 && moodPoint.getY() > 5) {
            moodPoint.setMood(MoodStatus.WRAITH);
        } else if(moodPoint.getX() > -5 && moodPoint.getX() < 5 && moodPoint.getY() > 5) {
            moodPoint.setMood(MoodStatus.CURIOSITY);
        } else if(moodPoint.getX() > 5 && moodPoint.getY() > 5) {
            moodPoint.setMood(MoodStatus.PASSION);
        } else if(moodPoint.getX() > 5 && moodPoint.getY() > -5 && moodPoint.getY() < 5) {
            moodPoint.setMood(MoodStatus.HAPPINESS);
        } else if(moodPoint.getX() > 5 && moodPoint.getY() < -5) {
            moodPoint.setMood(MoodStatus.SERENITY);
        } else if(moodPoint.getX() < 5 && moodPoint.getX() > -5 && moodPoint.getY() < 5) {
            moodPoint.setMood(MoodStatus.CAUTION);
        } else if(moodPoint.getX() < -5 && moodPoint.getY() < -5) {
            moodPoint.setMood(MoodStatus.ANNOYANCE);
        } else if(moodPoint.getX() >= -5 && moodPoint.getX() <= 0 && moodPoint.getY() <= 0 && moodPoint.getY() >= -5) {
            moodPoint.setMood(MoodStatus.WONDER);
        } else if(moodPoint.getX() >= 0 && moodPoint.getX() <= 5 && moodPoint.getY() >= -5 && moodPoint.getY() <= 0) {
            moodPoint.setMood(MoodStatus.CALM);
        } else if(moodPoint.getX() >= 0 && moodPoint.getX() <= 5 && moodPoint.getY() >= 0 && moodPoint.getY() <= 5) {
            moodPoint.setMood(MoodStatus.ENTHUSIASTIC);
        } else if(moodPoint.getX() >= -5 && moodPoint.getX() <= 0 && moodPoint.getY() >= 0 && moodPoint.getY() <= 5) {
            moodPoint.setMood(MoodStatus.ANGER);
        }
    }

    public boolean isBrainOverloaded() {
        return moodPoint.getX() > maxAxisValue ||
                moodPoint.getX() < minAxisValue ||
                moodPoint.getY() > maxAxisValue ||
                moodPoint.getY() < minAxisValue;
    }

    //TODO needs more work due to more complex logic
    public void changeMood(int xStep, int yStep) {
        moodPoint.setX(moodPoint.getX() + xStep);
        moodPoint.setY(moodPoint.getY() + yStep);
        updateMoodStatus();
    }


}

