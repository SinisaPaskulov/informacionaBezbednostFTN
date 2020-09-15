$(document).ready(function(){

    $('#loginSubmit').on('click', function(event) {
		event.preventDefault();
		var emailInput = $('#inputEmail');
        var passwordInput = $('#inputPassword');
		
		if($('#inputEmail').val() == "" || $('#inputPassword').val() == ""){
            alert('Bad input');
            return;
        }

        $.ajax({
            url: 'api/login',
            dataType: 'json',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify( { "username": emailInput.val(), "password": passwordInput.val() } ),
            success: function(response){
                sessionStorage.setItem("token", response.token);
                sessionStorage.setItem("email", response.email);
                sessionStorage.setItem("active_status", response.active_status);
                sessionStorage.setItem("role", response.role[0].name);
                window.location.href = 'index.html';
            },
            error: function( jqXhr, textStatus, errorThrown ){
                alert(jqXhr.responseText);
                console.log( errorThrown );
            }
        });
		
		/*$.post('api/login', {'email': email, 'password': password}.serialize(),
			function(response){
                window.location.href = 'index.html';
                console.log("Usnesna prijava")
				var userEmail = response.email;
				sessionStorage.setItem('userEmail', userEmail);
				if(response.authority.name == 'Admin'){
					window.location.href = 'index.html';
				}else {
					window.location.href = 'index.html';
				}
		}).fail(function(){
			console.log("error");
		});*/
	});

});