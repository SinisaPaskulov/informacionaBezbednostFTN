$(document).ready(function(){

    $('#registerSubmit').on('click', function(event) {
		event.preventDefault();
		var emailInput = $('#inputEmail');
        var passwordInput = $('#inputPassword');
        var repeatPassword = $('#repeatPassword');
		
		if(emailInput.val() == "" || passwordInput.val() == "" || repeatPassword.val() == "" || repeatPassword.val() != passwordInput.val() || emailInput.val().length < 4 || passwordInput.val() < 4){
            alert('Bad input');
            return;
        }

        $.ajax({
            url: 'api/registration',
            dataType: 'json',
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify( { "username": emailInput.val(), "password": passwordInput.val() } ),
            success: function(){
                alert('Registration successful');
                window.location.replace('login.html');
            },
            error: function( jqXhr, textStatus, errorThrown ){
                if(jqXhr.status == 201){
                    alert('Registration successful');
                    window.location.replace('login.html');
                }else{
                    alert(jqXhr.responseText);
                }
                console.log(jqXhr);
            }
        });
        event.preventDefault();
        return false;
    });
});