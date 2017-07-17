function submit() {
    var username = $("#username").val();
    $.get("/User/FreeName/"+ username,function (isFree) {
        if(!isFree){
            alert("Пользователь с наким именем уже существует");
        }else{
            var password = $("#password").val();
            var nickname = $("#nickname").val();
            $.post("/User/Register",
                {
                    username : username,
                    password : password,
                    nickname : nickname
                },
                function () {
                    $.post("/login",
                        {
                            username : username,
                            password : password
                        },
                        function () {
                            window.location.href = "/ContactBook";
                        })
                }
            )
        }
    })
}