package org.smartgresiter.wcaro.interactor;

import org.smartgresiter.wcaro.BuildConfig;
import org.smartgresiter.wcaro.job.ViewConfigurationsServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncSettingsServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;

import java.util.concurrent.TimeUnit;

public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void scheduleJobs() {
//        schedule jobs

        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig
                .DATA_SYNC_DURATION_MINUTES));

        PullUniqueIdsServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.PULL_UNIQUE_IDS_MINUTES), getFlexValue
                (BuildConfig.PULL_UNIQUE_IDS_MINUTES));

        ImageUploadServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.IMAGE_UPLOAD_MINUTES), getFlexValue(BuildConfig
                .IMAGE_UPLOAD_MINUTES));

        ViewConfigurationsServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.VIEW_SYNC_CONFIGURATIONS_MINUTES),
                getFlexValue(BuildConfig.VIEW_SYNC_CONFIGURATIONS_MINUTES));

        SyncSettingsServiceJob.scheduleJob(SyncSettingsServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.CLIENT_SETTINGS_SYNC_MINUTES),
                getFlexValue(BuildConfig.CLIENT_SETTINGS_SYNC_MINUTES));
    }

}
