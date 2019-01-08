package org.smartgresiter.wcaro.util;

import android.database.Cursor;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;
import org.smartgresiter.wcaro.application.WcaroApplication;
import org.smartgresiter.wcaro.interactor.ChildProfileInteractor;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.DBConstants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.helper.ECSyncHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChildUtils {
    private static final long MILLI_SEC=24 * 60 * 60 * 1000;//24 hrs
    private static final String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public  enum ONE_YR{ bcg,
        hepb,opv1,penta1,pcv1,rota1,opv2,penta2,pcv2,rota2,opv3,penta3,pcv3,rota3,ipv,mcv1,yf
        }
    public  enum TWO_YR{ bcg,
        hepb,opv1,penta1,pcv1,rota1,opv2,penta2,pcv2,rota2,opv3,penta3,pcv3,rota3,ipv,mcv1,yf,mcv2
    }
    public static String isFullyImmunized(int age, Map<String,Date> vaccineGiven){
        String str="";
        if(age>1 && age<2){
            List<TWO_YR> twoYrVac=Arrays.asList(TWO_YR.values());
            for( String vaccineName:vaccineGiven.keySet()){
                for (TWO_YR two_yr:twoYrVac){
                    if(two_yr.name().equalsIgnoreCase(vaccineName)){
                        str="Fully immunized at age 2" ;
                    }else{
                        str="Not Fully immunized at age 2" ;
                        break;
                    }
                }

            }
        }
        if(age<1){
            List<ONE_YR> oneYrVac=Arrays.asList(ONE_YR.values());
            for( String vaccineName:vaccineGiven.keySet()){
                for (ONE_YR one_yr:oneYrVac){
                    if(one_yr.name().equalsIgnoreCase(vaccineName)){
                        str="Fully immunized at age 1" ;
                    }else{
                        str="Not Fully immunized at age 1" ;
                        break;
                    }
                }

            }
        }
        return str;

    }
    //need to add primary caregiver filter at query
    //ec_family_member.is_primary_caregiver" is true
    public static String mainSelectRegisterWithoutGroupby(String tableName,String familyTableName,String familyMemberTableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, mainColumns(tableName,familyTableName,familyMemberTableName));
        queryBUilder.customJoin("LEFT JOIN " + familyTableName + " ON  " + tableName + ".relational_id =  " + familyTableName + ".id");
        queryBUilder.customJoin("LEFT JOIN " + familyMemberTableName + " ON  " + familyMemberTableName + ".relational_id =  " + familyTableName + ".id");


        String query=queryBUilder.mainCondition(mainCondition);
//      String query=" Select ec_child.id as _id , ec_child.relational_id as relationalid , \n" +
//              "ec_child.last_interacted_with , ec_child.base_entity_id , ec_child.first_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , \n" +
//              "ec_family.village_town as family_home_address , \n" +
//              "ec_child.last_name , ec_child.unique_id , ec_child.gender , ec_child.dob FROM ec_child,ec_family_member,ec_family \n" +
//              "where  ec_child.relational_id =  ec_family.id group by ec_child.base_entity_id ORDER BY ec_child.last_interacted_with DESC   LIMIT 0,20";

        return query;
    }
    public static String mainSelect(String tableName,String familyTableName,String familyMemberTableName, String mainCondition) {

       String  query=mainSelectRegisterWithoutGroupby(tableName,familyTableName,familyMemberTableName,tableName+"."+DBConstants.KEY.BASE_ENTITY_ID+" = '"+mainCondition+"'");


        return query;
    }
    public static String getChildListByFamilyId(String tableName,String familyId,String childId){
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName,new String[]{DBConstants.KEY.BASE_ENTITY_ID});
        String query=queryBUilder.mainCondition(tableName+"."+DBConstants.KEY.RELATIONAL_ID+" = '"+familyId+"'");
        return query;
    }
    public static String getLastHomeVisit(String tableName,String childId){
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName,new String[]{ChildDBConstants.KEY.LAST_HOME_VISIT,ChildDBConstants.KEY.VISIT_NOT_DONE});
        String query=queryBUilder.mainCondition(tableName+"."+DBConstants.KEY.BASE_ENTITY_ID+" = '"+childId+"'");
        return query;
    }
    private static String[] mainColumns(String tableName,String familyTable,String familyMemberTable) {

        String[] columns = new String[]{
                tableName + "." + DBConstants.KEY.RELATIONAL_ID +" as " +ChildDBConstants.KEY.RELATIONAL_ID,
                tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                tableName + "." + DBConstants.KEY.FIRST_NAME,
                familyMemberTable + "."+DBConstants.KEY.FIRST_NAME+" as "+ChildDBConstants.KEY.FAMILY_FIRST_NAME,
                familyMemberTable + "."+DBConstants.KEY.LAST_NAME+" as "+ChildDBConstants.KEY.FAMILY_LAST_NAME,
                familyTable + "."+DBConstants.KEY.VILLAGE_TOWN+" as "+ChildDBConstants.KEY.FAMILY_HOME_ADDRESS,
                tableName + "." + DBConstants.KEY.LAST_NAME,
                tableName + "." + DBConstants.KEY.UNIQUE_ID,
                tableName + "." + DBConstants.KEY.GENDER,
                tableName + "." + DBConstants.KEY.DOB,
                tableName + "." + ChildDBConstants.KEY.LAST_HOME_VISIT,
                tableName + "." + ChildDBConstants.KEY.VISIT_NOT_DONE};
        return columns;
    }
    public static ChildVisit getChildVisitStatus(long lastVisitDate,boolean isVisitNotDone){
        ChildVisit childVisit=new ChildVisit();
        childVisit.setLastVisitTime(lastVisitDate);
        childVisit.setVisitNotDone(isVisitNotDone);
        //testing data
        //childVisit.setLastVisitTime(1545867630000L);


        long diff=System.currentTimeMillis()-childVisit.getLastVisitTime();
        if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1){
            childVisit.setVisitStatus(ChildProfileInteractor.VisitType.DUE.name());
            if(childVisit.getLastVisitTime()!=0){
                if(diff<MILLI_SEC){
                    childVisit.setLastVisitDays("less 24 hrs");
                }else{
                    childVisit.setLastVisitDays(diff/MILLI_SEC+" days");
                }
            }
            return childVisit;
        }
        if(childVisit.getLastVisitTime()==0 && !childVisit.isVisitNotDone()){
            childVisit.setVisitStatus(ChildProfileInteractor.VisitType.OVERDUE.name());
            return childVisit;
        }

        if(diff<MILLI_SEC){
            childVisit.setLastVisitDays("less 24 hrs");
            childVisit.setVisitStatus(ChildProfileInteractor.VisitType.LESS_TWENTY_FOUR.name());
            childVisit.setLastVisitMonth(theMonth(Calendar.getInstance().get(Calendar.MONTH)));
            return childVisit;
        }
        if(isVisitNotDone){
            childVisit.setVisitStatus(ChildProfileInteractor.VisitType.NOT_VISIT_THIS_MONTH.name());
            return childVisit;
        }
        else {
            childVisit.setLastVisitDays(diff/MILLI_SEC+" days");
            childVisit.setVisitStatus(ChildProfileInteractor.VisitType.OVER_TWENTY_FOUR.name());
            return childVisit;
        }
    }
    public static boolean isSameMonth(long time){
        if(time==0) return false;
        int runningMonth=Calendar.getInstance().get(Calendar.MONTH);
        int runningYear=Calendar.getInstance().get(Calendar.YEAR);
        Date date = new Date(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int visitMonth=cal.get(Calendar.MONTH);
        int visitYear=cal.get(Calendar.YEAR);
        if(runningMonth==visitMonth && runningYear==visitYear){
            return true;
        }
        return false;
    }
    public static String theMonth(int month){
        return monthNames[month];
    }
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }
    //event type="Child Home Visit"/Visit not done
    public static void updateClientStatusAsEvent(String entityId,String eventType,String attributeName, Object attributeValue, String entityType) {
        try {

            ECSyncHelper syncHelper = FamilyLibrary.getInstance().getEcSyncHelper();

            Event event = (Event) new Event()
                    .withBaseEntityId(entityId)
                    .withEventDate(new Date())
                    .withEventType(eventType)
                    .withLocationId(WcaroApplication.getInstance().getContext().allSharedPreferences().fetchCurrentLocality())
                    .withProviderId(WcaroApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM())
                    .withEntityType(entityType)
                    .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(new Date());
            event.addObs((new Obs()).withFormSubmissionField(attributeName).withValue(attributeValue).withFieldCode(attributeName).withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<Object>()));

            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
            syncHelper.addEvent(entityId, eventJson);
            long lastSyncTimeStamp = WcaroApplication.getInstance().getContext().allSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            FamilyLibrary.getInstance().getClientProcessorForJava().processClient(syncHelper.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            WcaroApplication.getInstance().getContext().allSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());

            //update details



        } catch (Exception e) {
            Log.e("Error in adding event", e.getMessage());
        }
    }
}
