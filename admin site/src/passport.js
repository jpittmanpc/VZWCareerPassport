fbUrl = "vzw.firebaseio.com";
db = new Firebase(fbUrl);
update= {
    user: function(key, query) {
        $("#firstload").hide();
        $(".addusers").hide();
        $("#dynamicContent").show();
        var temp = Handlebars.compile(UserDiv)
        query['percent'] = "50";
        query['key'] = key;
        if (presence[key]) {
            query["online"] = presence[key];
            if (query["online"] != "Online") {
                a = new Date(presence[key]);
                query.online = "Last seen: " + ((a.getMonth()) + 1) + "-" + a.getDate() + "-" + a.getFullYear();
            }
        }

        if (query.achievements) {
                 query['percent'] = Math.floor(((query.achievements.length) -1) * 10);
                 query['achupdate'] = "<select class='ach' multiple>";
                 i=0;
                for (z=0;z<10;z++) {
                    query['achupdate'] = query['achupdate'] + "<option value=" + i + ">" + z + "</option>";
                }
                query['achupdate'] = query['achupdate'] + "</select>";
                console.log(query['achupdate']);
        }
        else { query['percent'] = 0; }
        $("#dynamicContent").html(temp(query));
        $("button#" + key).on("click", function() {
             data = $(this).attr("data");
             if (data == 'ach') {
                update.achievements(key);
                console.log($(this).next());
             }
             console.log(data);
        });
    },
    achievements: function(key) {
        $("#pct").hide();
        $("#ach").show();
        $("select.ach").css({'width': '100%'});
    },
    userlist: function(query) {
        if (query == "admin") {
            var temp = Handlebars.compile(UserList);
            userReference = new Object();

            db.child('users').once('value', function(data) {
                data.forEach(function(snap) {
                    key = snap.key(); name=snap.val().name;
                    if (snap.val().admin == true) {
                        userReference[key] = name;
                    }
                });
            });
            $("#users").html(temp(userReference));
        }

    },
    first: function() {
                       context={
                            firstload: {"title":"Quick Snapshot"},
                            "totalusers":"1",
                            "highestpct":"Test user"
                        };
            var temp = Handlebars.compile(firstLoad);
            $("#firstload").html(temp(context)).show();
            $("#dynamicContent").html("");
    },

}

window.onload = function() {
    if (db.getAuth() == null) {
        $(".header").html(Handlebars.compile(Header));
        $("#content").hide();
        $("#Login").on("click", function() {
            $('div.text').text("validating..");
            db.authWithPassword({
                "email": $("input.email").val(),
                "password": $("input.pw").val()
            },
            function(data) {
                if (data != null) {
                    $('div.text').text(data.message);
                } else {
                    var UID = db.getAuth().uid;
                    $('#login').animate({
                        height: 'toggle'
                    });
                    start();
                    $('#content').fadeIn("300");

                }
            });
        });
    } else {
        start();
    }
}
function start() {
        getUsers();
          if (db.getAuth()) {
             temp=Handlebars.compile(Header);
            context={"logout":"<button id='logout'>Logout</button>"};
            $(".header").html(temp(context));
            $("#logout").on("click", function() {
                db.unauth();
                $("#content").hide();location.reload();
            });
        }
        $("#login").hide();
        $("button").on("click", function() {
            $(".addusers a#text").text("");
            $("#dynamicContent").html("");
            if (this.textContent === "Add Users") {
                    $("#firstload").hide();
                    $("#dynamicContent").hide();
                    $(".addusers").show();
            }
            if (this.textContent == "Add User") {
                createUser();
            }
            if (this.textContent == "Remove User") {
                alert("not working yet :D");
            }
            else {
                console.log(this.textContent);
            }
        });
        db.child(".info/connected").on('value', function(data) {
            if (data.val()) {
                presenceserver = db.child('presence').child(db.getAuth().uid);
                console.log("Connected");
                presenceserver.onDisconnect().set(Firebase.ServerValue.TIMESTAMP);
                presenceserver.set("Online");
            }
            else { 
                console.log("Disconnected");
            }
        });
}

function createUser() {
    var quit = false;
    $(".addusers div").each(function() {
        if ($(this).find('input')[0]) {
            Text = $(this).text().toLowerCase().slice(0, -2);
            if (!$(this).find('input').val()) {
                $('.addusers a#text').html("<b>" + Text + "</b> field is missing.");
                quit = true;;
            }
        }
    });
    if (quit != true || quit == null) {
        $('.addusers a#text').html("Creating User...");
        db.createUser({
            "email": $(".addusers input#e-mail").val(),
            "password": $(".addusers input#password").val()
        },
        function(error, success) {
            if (success) {
                console.log(success);
                userUID = success.uid;
                $('.addusers a#text').html("Setting Data...");
                $("div.addusers div.rel").each(function() {
                    if ($(this).find('input')[0]) {
                        db.child('users').child(userUID).child($(this).text().toLowerCase().split(":")[0])
                        .set($(this).find('input').val())
                    }
                });
                $('.addusers a#text').html("Added " + $(".addusers input#name").val() + "!");
            }
            if (error) {
                $('.addusers a#text').html(" " + error);
                return;
            }
                        //else { $('.addusers a#text').html("Error..." + error + " " + success); }
                    });
    } else {
        $('.addusers a#text').html("Error...");
    }
}

function getUsers() {
    $("#users").html("");
    db.child("presence").once("value",function(b) {
        presence = b.val();
    });
    db.child("presence").on("child_changed",function(data) {
        presence[data.key()] = data.val();
    });
    db.child('users').on("child_added", function(data) {
        dataval = data.val();
        if (dataval.name && !dataval.admin) {
            htmlentry = "<option value=" + data.key() + ">" + dataval.name + "</option>";
            $('select#users').append(htmlentry);
        } else {}
    });

    $("select#users").on('click', function() {
        db.child('users').child(this.value).once("value", function(data) {
            update.user(data.key(), data.val());
            $('#pct').circliful();
        });
     });
};

function upper(a) {
    return a.toUpperCase();
}

function get(name) {
    return document.getElementById(name);
}
//Here I am catching all of the elements that will change.
(function() {
    Header = $(".header").html();
    UserDiv = $("#dynamicContent").html()
    UserList = $("#userholder").html()
    firstLoad = $("#firstload").html();
    //Removing {{this}} template from userlist.
    $("#users option")[0].remove();
    //Function for "Quick Snapshot"
    update.first();
    //Clearing out this element.
    $("#dynamicContent").html("");
    //Setting up the select for onClick.
    $("select.choose").on("click",function() { if (this.value == "Admins") { update.userlist("admin"); } else { getUsers(); } });
})();
//For Circle Progress:
(function(t){t.fn.circliful=function(i){var e=t.extend({startdegree:0,fgcolor:"#556b2f",bgcolor:"#eee",fill:!1,width:15,dimension:200,fontsize:15,percent:50,animationstep:1,iconsize:"20px",iconcolor:"#999",border:"default",complete:null,bordersize:10},i);return this.each(function(){function a(i,e,a){t("<span></span>").appendTo(i).addClass(e).text(s).prepend(l).css({"line-height":a+"px","font-size":h.fontsize+"px"})}function n(i,e){t("<span></span>").appendTo(i).addClass("circle-info-half").css("line-height",h.dimension*e+"px").text(r)}function o(i){t.each(c,function(a,n){h[n]=void 0!=i.data(n)?i.data(n):t(e).attr(n),"fill"==n&&void 0!=i.data("fill")&&(m=!0)})}function d(e){u.clearRect(0,0,g.width,g.height),u.beginPath(),u.arc(x,b,w,z,y,!1),u.lineWidth=h.width+1,u.strokeStyle=h.bgcolor,u.stroke(),m&&(u.fillStyle=h.fill,u.fill()),u.beginPath(),u.arc(x,b,w,-k+T,C*e-k+T,!1),"outline"==h.border?u.lineWidth=h.width+13:"inline"==h.border&&(u.lineWidth=h.width-13),u.strokeStyle=h.fgcolor,u.stroke(),p>M&&(M+=I,requestAnimationFrame(function(){d(Math.min(M,p)/100)},f)),M==p&&S&&i!==void 0&&t.isFunction(i.complete)&&(i.complete(),S=!1)}var s,r,c=["fgcolor","bgcolor","fill","width","dimension","fontsize","animationstep","endPercent","icon","iconcolor","iconsize","border","startdegree","bordersize"],h={},l="",p=0,f=t(this),m=!1;if(f.addClass("circliful"),o(f),void 0!=f.data("text")&&(s=f.data("text"),void 0!=f.data("icon")&&(l=t("<i></i>").addClass("fa "+t(this).data("icon")).css({color:h.iconcolor,"font-size":h.iconsize})),void 0!=f.data("type")?(F=t(this).data("type"),"half"==F?a(f,"circle-text-half",h.dimension/1.45):a(f,"circle-text",h.dimension)):a(f,"circle-text",h.dimension)),void 0!=t(this).data("total")&&void 0!=t(this).data("part")){var v=t(this).data("total")/100;percent=(t(this).data("part")/v/100).toFixed(3),p=(t(this).data("part")/v).toFixed(3)}else void 0!=t(this).data("percent")?(percent=t(this).data("percent")/100,p=t(this).data("percent")):percent=e.percent/100;void 0!=t(this).data("info")&&(r=t(this).data("info"),void 0!=t(this).data("type")?(F=t(this).data("type"),"half"==F?n(f,.9):n(f,1.25)):n(f,1.25)),t(this).width(h.dimension+"px");var g=t("<canvas></canvas>").attr({width:h.dimension,height:h.dimension}).appendTo(t(this)).get(0),u=g.getContext("2d");t(g).parent();var x=g.width/2,b=g.height/2,P=360*h.percent;P*(Math.PI/180);var w=g.width/2.5,y=2.3*Math.PI,z=0,M=0===h.animationstep?p:0,I=Math.max(h.animationstep,0),C=2*Math.PI,k=Math.PI/2,F="",S=!0,T=h.startdegree/180*Math.PI;void 0!=t(this).data("type")&&(F=t(this).data("type"),"half"==F&&(y=2*Math.PI,z=3.13,C=Math.PI,k=Math.PI/.996)),d(M/100)})}})(jQuery);