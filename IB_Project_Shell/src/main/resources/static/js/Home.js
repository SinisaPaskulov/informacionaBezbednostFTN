$(document).ready(function(){

    var showProfileInfo = false;

    var role = sessionStorage.getItem("role");
    var token = sessionStorage.getItem("token");
    var email = sessionStorage.getItem("email");
    var active_status = sessionStorage.getItem("active_status");

    $("#profileIcon").click(function (e){
        showProfileInfo = !showProfileInfo;
        if(showProfileInfo){
            $("#profile_info").css("display","initial");
        }else{
            $("#profile_info").css("display","none");
        }
        e.preventDefault();
        return false;
    });

    function changeInterface(){
        $("#userEmail").text(email);
        if(active_status == "true"){
            $("#userStatus").text("status: ACTIVE");
            $("#manageUsers").css("display","initial");
            $("#downloadJksButton").css("display","initial");
            getUsers();
        }else{
            $("#userStatus").text("status: DISABLED");
        }
        $("#userRole").text("role: " + role);
    };

    $("#logoutButton").click(function (e){
      $.post('api/logout',
			function(response){
        sessionStorage.clear();
        window.location.href = 'login.html';
		}).fail(function(response){
			console.log(response.text);
		});

      e.preventDefault();
      return false;
    });

    function getUsers(){
        $.ajax({
            url: 'api/users',
            dataType: 'json',
            type: 'get',
            contentType: 'application/json',
            headers: {
                "X-Auth-Token": token,
            },
            success: function(response){
                var users = response;
                drawTable(users)
                console.log(response);

            },
            error: function( jqXhr, textStatus, errorThrown ){
                console.log( errorThrown );
            }
        });
    }

    function drawTable(users){
      $("#tableUsers").find('tbody').remove();     
        if(role == "ADMIN"){
          var tr = document.getElementById('tableUsers').tHead.children[0],
          th = document.createElement('th');
          th.innerHTML = "User Activation";
          th.scope = "col";
          tr.appendChild(th);
          for(it in users){
          $("#tableUsers").append(
            '<tbody>' +
              '<tr>' + 
                '<th scope="row" align="left">' + users[it].id + '</th>' + 
                '<td align="left">' + users[it].email + '</td>' +
                '<td align="left">' + users[it].active + '</td>' + 
                '<td align="left">' + users[it].authorities[0].name + '</td>' + 
                '<td align="left">'+ '<button class="downloadCertificateBtn" id='+ users[it].email +' style="background-color:rgb(237, 28, 36);color:white;padding: 3px;border: 1px solid #606060;">Download</button>' +'</td>' + 
                '<td align="left">'+ '<button class="activateUserBtn" id='+ users[it].id +' style="background-color:rgb(237, 28, 36);color:white;padding: 3px;border: 1px solid #606060;">Activate</button>' +'</td>' +
              '</tr>' +
            '</tbody>'
            )}
        }else{
          for(it in users){
          $("#tableUsers").append(
            '<tbody>' +
              '<tr>' + 
                '<th scope="row" align="left">' + users[it].id + '</th>' + 
                '<td align="left">' + users[it].email + '</td>' +
                '<td align="left">' + users[it].active + '</td>' + 
                '<td align="left">' + users[it].authorities[0].name + '</td>' + 
                '<td align="left">'+ '<button class="downloadCertificateBtn" id='+ users[it].email +' style="background-color:rgb(237, 28, 36);color:white;padding: 3px;border: 1px solid #606060;">Download</button>' +'</td>' + 
              '</tr>' +
            '</tbody>'
            )
          }
      }
          $(".activateUserBtn").click(function (e){
            activateUser(this.id);
            e.preventDefault();
            return;
          });
          $(".downloadCertificateBtn").click(function (e){
            downloadCertificate(this.id);
            e.preventDefault();
            return;
          });
    }

    function activateUser(id){
      $.ajax({
        url: 'api/activate_user?id=' + id,
        dataType: 'json',
        type: 'post',
        contentType: 'application/json',
        headers: {
            "X-Auth-Token": token,
        },
        success: function(response){
          $("#tableUsers").find('tbody').remove();
          getUsers();
          alert('User activation successful');
          return;
        },
        error: function( jqXhr, textStatus, errorThrown ){
          if(jqXhr.status == 201){
            $("#tableUsers").find('tbody').remove();
            getUsers();
            alert('User activation successful');
            return;
            }
            console.log( jqXhr );
            alert(jqXhr.responseText);
            return;
        }
    });
    };

    $("#searchUserButton").click(function (e){
      var email = $("#searchUserInput").val();
      if(email != null && email != "" && !email.length < 4){
        $.ajax({
          url: 'api/findUser',
          dataType: 'json',
          type: 'get',
          contentType: 'application/json',
          headers: {
              "X-Auth-Token": token,
              "email":email
          },
          success: function(response){
              var users = response;
              drawTable(users)
              console.log(response);
  
          },
          error: function( jqXhr, textStatus, errorThrown ){
              console.log( errorThrown );
              alert(jqXhr.responseText);
          }
      });
      }
      e.preventDefault();
      return;
    });

    $("#downloadJksButton").click(function (e){
      if(active_status == "true"){
        downloadJKS(email);
      }
      e.preventDefault();
      return;     
    });

    function downloadJKS(email){
      if(email != null && email != "" && !email.length < 4){
        var xhr = $.ajax({
          url: 'api/downloadJks',
          type: 'get',
          xhrFields: {
            responseType: 'blob'
          },
          headers: {
              "X-Auth-Token": token,
              "email":email
          },
          success: function(response, status){
            var a = document.createElement('a');
            var url = window.URL.createObjectURL(response);
            a.href = url;
            a.download = xhr.getResponseHeader("filename");
            document.body.append(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
          },
          error: function( jqXhr, textStatus, errorThrown ){
              console.log( errorThrown );
          }
      });
    }
    };

    function downloadCertificate(email){
      if(email != null && email != "" && !email.length < 4){
        var xhr = $.ajax({
          url: 'api/downloadCertificate',
          type: 'get',
          xhrFields: {
            responseType: 'blob'
          },
          headers: {
              "X-Auth-Token": token,
              "email":email
          },
          success: function(response, status){
            var a = document.createElement('a');
            var url = window.URL.createObjectURL(response);
            a.href = url;
            a.download = xhr.getResponseHeader("filename");
            document.body.append(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
          },
          error: function( jqXhr, textStatus, errorThrown ){
              console.log( errorThrown );
          }
      });
    }
    };

    changeInterface();

});