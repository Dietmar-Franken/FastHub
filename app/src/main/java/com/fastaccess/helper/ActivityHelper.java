package com.fastaccess.helper;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.fastaccess.R;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Created by Kosh on 12/12/15 10:51 PM
 */
public class ActivityHelper {

    @Nullable public static Activity getActivity(@Nullable Context content) {
        if (content == null) return null;
        else if (content instanceof Activity) return (Activity) content;
        else if (content instanceof ContextWrapper) return getActivity(((ContextWrapper) content).getBaseContext());
        return null;
    }

    public static void login(@NonNull Activity activity, @NonNull Uri url) {
        try {
            Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toasty.info(activity, "Chrome is required to login").show();
            e.printStackTrace();
        }
    }

    public static void startCustomTab(@NonNull Activity context, @NonNull Uri url) {
        String packageNameToUse = CustomTabsHelper.getPackageNameToUse(context);
        if (packageNameToUse != null) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setToolbarColor(ViewHelper.getPrimaryColor(context))
                    .setShowTitle(true)
                    .build();
            customTabsIntent.intent.setPackage(packageNameToUse);
            customTabsIntent.launchUrl(context, url);
        } else {
            openChooser(context, url, true);
        }
    }

    public static void startCustomTab(@NonNull Activity context, @NonNull String url) {
        startCustomTab(context, Uri.parse(url));
    }

    public static void openChooser(@NonNull Context context, @NonNull Uri url) {
        openChooser(context, url, false);
    }

    private static void openChooser(@NonNull Context context, @NonNull Uri url, boolean fromCustomTab) {
        Intent i = new Intent(Intent.ACTION_VIEW, url);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent finalIntent = chooserIntent(context, i, url);
        if (finalIntent != null) {
            context.startActivity(finalIntent);
        } else {
            if (!fromCustomTab) {
                Activity activity = ActivityHelper.getActivity(context);
                if (activity == null) {
                    context.startActivity(i);
                    return;
                }
                startCustomTab(activity, url);
            } else {
                context.startActivity(i);
            }
        }
    }

    public static void openChooser(@NonNull Context context, @NonNull String url) {
        openChooser(context, Uri.parse(url));
    }

    @SafeVarargs public static void start(@NonNull Activity activity, Class cl, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(activity, cl);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements);
        activity.startActivity(intent, options.toBundle());
    }

    public static void start(@NonNull Activity activity, Intent intent, @NonNull View sharedElement) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                sharedElement, ViewHelper.getTransitionName(sharedElement));
        activity.startActivity(intent, options.toBundle());
    }

    public static void startReveal(@NonNull Activity activity, Intent intent, @NonNull View sharedElement, int requestCode) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeClipRevealAnimation(sharedElement, sharedElement.getWidth() / 2,
                sharedElement.getHeight() / 2,
                sharedElement.getWidth(), sharedElement.getHeight());
        activity.startActivityForResult(intent, requestCode, options.toBundle());
    }

    public static void startReveal(@NonNull Fragment fragment, Intent intent, @NonNull View sharedElement, int requestCode) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeClipRevealAnimation(sharedElement, sharedElement.getWidth() / 2,
                sharedElement.getHeight() / 2,
                sharedElement.getWidth(), sharedElement.getHeight());
        fragment.startActivityForResult(intent, requestCode, options.toBundle());
    }

    public static void startReveal(@NonNull Activity activity, Intent intent, @NonNull View sharedElement) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeClipRevealAnimation(sharedElement, sharedElement.getWidth() / 2,
                sharedElement.getHeight() / 2,
                sharedElement.getWidth(), sharedElement.getHeight());
        activity.startActivity(intent, options.toBundle());
    }

    @SafeVarargs public static void start(@NonNull Activity activity, @NonNull Intent intent, @NonNull Pair<View, String>... sharedElements) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements);
        activity.startActivity(intent, options.toBundle());

    }

    public static void shareUrl(@NonNull Context context, @NonNull String url) {
        Activity activity = getActivity(context);
        if (activity == null) throw new IllegalArgumentException("Context given is not an instance of activity " + context.getClass().getName());
        try {
            ShareCompat.IntentBuilder.from(activity)
                    .setChooserTitle(context.getString(R.string.share))
                    .setType("text/*")
                    .setText(url)
                    .startChooser();
        } catch (ActivityNotFoundException e) {
            Toasty.error(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Nullable public static Fragment getVisibleFragment(@NonNull FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();
        if (fragments != null && !fragments.isEmpty()) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible()) {
                    return fragment;
                }
            }
        }
        return null;
    }

    private static boolean isPermissionGranted(@NonNull Context context, @NonNull String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isExplanationNeeded(@NonNull Activity context, @NonNull String permissionName) {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permissionName);
    }

    private static boolean isReadWritePermissionIsGranted(@NonNull Context context) {
        return isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                && isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private static void requestReadWritePermission(@NonNull Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 1);
    }

    public static boolean checkAndRequestReadWritePermission(@NonNull Activity activity) {
        if (!isReadWritePermissionIsGranted(activity)) {
            requestReadWritePermission(activity);
            return false;
        } else if (isExplanationNeeded(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                || isExplanationNeeded(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toasty.error(activity, activity.getString(R.string.read_write_permission_explanation), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private static Intent chooserIntent(@NonNull Context context, @NonNull Intent intent, @NonNull Uri uri) {
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        final ArrayList<Intent> chooserIntents = new ArrayList<>();
        final String ourPackageName = context.getPackageName();
        for (ResolveInfo resInfo : activities) {
            ActivityInfo info = resInfo.activityInfo;
            if (!info.enabled || !info.exported) {
                continue;
            }
            if (info.packageName.equals(ourPackageName)) {
                continue;
            }
            Intent targetIntent = new Intent(intent);
            targetIntent.setPackage(info.packageName);
            targetIntent.setDataAndType(uri, intent.getType());
            chooserIntents.add(targetIntent);
        }
        if (chooserIntents.isEmpty()) {
            return null;
        }
        final Intent lastIntent = chooserIntents.remove(chooserIntents.size() - 1);
        if (chooserIntents.isEmpty()) {
            return lastIntent;
        }
        Intent chooserIntent = Intent.createChooser(lastIntent, null);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, chooserIntents.toArray(new Intent[chooserIntents.size()]));
        return chooserIntent;
    }


}
