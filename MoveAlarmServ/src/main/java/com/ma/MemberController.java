package com.ma;


import com.google.gson.JsonObject;
import com.ma.model.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 10/15/2015.
 * Last modified 10/15/2015.
 */
@RestController
public class MemberController {

        MemberDbDriver jdbc = MemberJDBC.getInstance();

        @RequestMapping("/connect")
        public MemberDbDriver connect(){
            return jdbc;
        }

        @RequestMapping("/getMember")
        public Member getMemberByID(@RequestParam(value = "userID",required = false)
                                        String userID){
            int pk = 0;
            try{
                pk = Integer.parseInt(userID);
            }catch (Exception e){//catch format
                System.err.println(e);
            }
            Member member = new Member();
            member.setPk(pk);
            Boolean found = jdbc.getMemberData(member);
            return (found) ? member : null;
        }


        @RequestMapping(value = "/getFriendList", method = RequestMethod.POST)
        public List<Member> getFriendListID(@RequestBody ArrayList<String> listID){
            ArrayList<Member> list = new ArrayList<>();
            for (String aListID : listID) {
                System.out.println(aListID);
                int pk = jdbc.getPk(Long.parseLong(aListID));
                Member member = getMemberByID(pk + "");
                System.out.println(member);
                if(member!=null)
                    list.add(member);
            }
            if(!list.isEmpty()) {
                LeaderBoard leaderBoard = new LeaderBoard(list);
                return leaderBoard.getLeaderboard();
            }else
                return null;
        }



        @RequestMapping(value = "/regMember",method=RequestMethod.POST)
        public String regMember(@RequestBody(required = false) Member member){
            JsonObject jo = new JsonObject();
            int pk;
            String status;
            System.out.println(member.getPk());

            if(member.getIdFb() != 0L){
                pk = jdbc.getPk(member.getIdFb());
                member.setPk(pk);
                if(pk == -1)
                    pk = jdbc.insertMember(member);
                else
                    pk = jdbc.updateMember(member);
                status = (pk != -1)?"Success ":"Failed";
            }
            else {
                pk = -2;//non valid code
                status = "Form not valid";
            }
            jo.addProperty("pk",pk);
            jo.addProperty("status",status);
            System.out.println(jo.toString());//check result in server
            return jo.toString();
        }

        @RequestMapping(value = "/updateStatus",method = RequestMethod.POST)
        public String UpdateStatus(@RequestBody(required = false) Member member){
            JsonObject jo = new JsonObject();
            int pk;
            String result;
            boolean found;
            if(member.getStatus() != null & member.getPk() != 0) {
                pk = member.getPk();
                String status = member.getStatus();
                found = jdbc.updateStatus(pk,status);
                result = "Form OK";
                result += (found)?" Successful":" Failed";
            }else{
                pk = -2;//non valid code
                result = "Form not valid";
            }
            jo.addProperty("pk",pk);
            jo.addProperty("status",result);
            System.out.println(jo.toString());//check result in server
            return jo.toString();
        }

        @RequestMapping("/addPoint")
        public String increasePoint(@RequestParam(value = "exID",defaultValue = "0") int exID ,
                                  @RequestParam(value = "userID") String id){
            Member member = getMemberByID(id);
            if(member.getPk() != 0 & exID != 0){
                ScoreCalculator s = ScoreCalculator.getInstance();
                int newScore = s.addScore(member.getScore(),exID);
                jdbc.updatePoint(id,newScore);
                JsonObject json = new JsonObject();
                json.addProperty("userID",id);
                json.addProperty("newScore",newScore);
                return json.toString();
            }else {
                System.out.println("form invalid");
                return (new JsonObject()).toString();
            }
        }
}
