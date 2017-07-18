var CONTACTS;

$.ajax({
        type: 'GET',
        url: '/ContactBook/contacts',
        data: {get_param: 'value'},
        dataType: 'json',
        success: function (data) {
            CONTACTS = data;
            setContacts(data);

        }
    }
);

function setContacts(data) {
    $.each(data, function (g_i, group) {
        var contactsList = addGroupHeaderElement(group).children("ul");
        $.each(group.members, function (m_i, member) {
            addContactElement(member, contactsList);
        });
    });
    $(document).ready(function () {
        $(".ul").hide();
    });
}

function removeGroup(id) {
    $.get("/ContactBook/removeGroup/"+id,
        function () {
            $("#group"+id).parent("div").remove();
        }
    );
}

function addMember(groupId) {
    $.get("/ContactBook/addContact/"+groupId,
        function (contactId) {
            var contact = createContact(contactId);
            $.each(CONTACTS, function (g_i, group){
                if(group.id==groupId){
                    group.members.push(contact);
                }
            });
            addContactElement(contact,$("#group"+groupId));
            setInfo(contact);
        }
    )
}

function createContact(id) {
    return {
        id:id,
        nick:'new',
        firstName:'',
        lastName:'',
        number:'',
        email:'',
        fieldValues:[]
    }
}

function addContactElement(member, contactsList) {
    var contactElement = $("<li class='li' id='nick_li_"+member.id+"'><a id='nick"+member.id+"' href='#' >" + member.nick + "</a></li>");
    contactElement.click(function () { setInfo(member) });
    contactElement.appendTo(contactsList);
}

function addGroup() {
    var newGroupName = $('#new_group').val();
    if (newGroupName == "") {
        alert("Вы не ввели название группы!");
    } else {
        $.get("/ContactBook/addGroup/"+newGroupName,
            function (groupId) {
                var newGroup = {
                    groupId:groupId,
                    groupName:newGroupName,
                    members:[]
                };
                CONTACTS.push(newGroup);
                addGroupHeaderElement(newGroup);
            }
        )
    }
}

function addGroupHeaderElement(group) {
    var groupBox = $("<div class='box2'></div>");

    var addMemberBtn = $("<button class='submit tree' >+</button>");
    addMemberBtn.click( function () { addMember(group.groupId) });
    groupBox.append(addMemberBtn);

    var removeGroupBtn = $("<button class='clear2 tree'>X</button>");
    removeGroupBtn.click( function () { removeGroup(group.groupId) });
    groupBox.append(removeGroupBtn);

    var groupHeader = $("<h3>"+group.groupName+"</h3>");
    groupBox.append(groupHeader);

    var expandGroup = $("<span  class='expand'>+</span>");
    groupHeader.append(expandGroup);

    var contactsList = $("<ul id='group" + group.groupId + "' class='ul'></ul>");
    groupBox.append(contactsList);

    expandGroup.click( function () { contactsList.slideToggle() });

    $('#results').append(groupBox);

    return groupBox;
}

function setInfo(member) {
    $('#selectedId').val(member.id);
    $('#nickname').val(member.nick);
    $('#firstname').val( member.firstName);
    $('#lastname').val( member.lastName);
    $('#number').val( member.number);
    $('#email').val( member.email);
    $.each(member.fieldValues, function (fv_i, field) {
        addFieldElement(field);
    })
}

function addField() {
    var newFieldName = $('#new_field').val();
    if (newFieldName == "") {
        alert("Вы не ввели название поля!");
    } else {
        $.get("/ContactBook/fieldId/"+newFieldName,
            function (fieldId) {
                var userId = $("#selectedId").val();
                $.get("/ContactBook/setFieldValue/"+userId+"/"+fieldId,
                function (fieldValueId) {
                    var member = getMember(userId);
                    var field = {
                        id:fieldValueId,
                        userId:userId,
                        fieldId:fieldId,
                        fieldName:newFieldName,
                        value:''
                    };
                    member.fieldValues.push(field);
                    addFieldElement(field);
                })
            });
    }
}

function addFieldElement(field) {
    var newFieldElement = $("<li id='field"+field.id+"'><input type='hidden' class='fieldId' value='"+field.id+"'>" +
                                "<label>" + field.fieldName + ":</label><input id='fieldValueInput"+field.id+"' class='in'/></li>");
    var removeFieldBtn = $("<button class='clear2'>X</button>");
    removeFieldBtn.click(function () {
        $.get("/ContactBook/removeFieldValue/"+field.id,
            function () {
                removeField(field.id);
            });
    });
    newFieldElement.append(removeFieldBtn);
    $("#last").append(newFieldElement);
}

function removeField(fieldId) {
    $("#field"+fieldId).remove();
}

function removeContact() {
    var id = $("#selectedId").val();
    $.get("/ContactBook/removeContact/"+ id,
    function () {
        $("#nick_li_"+id).remove();
        $("#fields_ul li input").val('');
    })
}

jQuery(document).ready(function () {
    var offset = 80;
    var duration = 500;
    jQuery(window).scroll(function () {
        if (jQuery(this).scrollTop() > offset) {
            jQuery('.scroll-to-top').fadeIn(duration);
        } else {
            jQuery('.scroll-to-top').fadeOut(duration);
        }
    });

    jQuery('.scroll-to-top').click(function (event) {
        event.preventDefault();
        jQuery('html, body').animate({scrollTop: 0}, duration);
        return false;
    })
});

function submit() {
    var data = {
        id:$("#selectedId").val(),
        nick:$("#nickname").val(),
        firstName:$("#firstname").val(),
        lastName:$("#lastname").val(),
        number:$("#number").val(),
        email:$("#email").val(),
        fieldValues:[]
    };
    var member = getMember(data.id);
    $.each(member.fieldValues, function (fv_i, fieldValue) {
        data.fieldValues.push({
            id:fieldValue.id,
            userId:fieldValue.userId,
            fieldId:fieldValue.fieldId,
            fieldName:fieldValue.fieldName,
            value:$("#fieldValueInput"+fieldValue.id).val()
        });
    });
    $.ajax("/ContactBook/update",{
        method:'POST',
        data:JSON.stringify(data),
        contentType : 'application/json',
        success : function () {
            member.nick = data.nick;
            member.firstName = data.firstName;
            member.lastName = data.lastName;
            member.number = data.number;
            member.email = data.email;
            $.each(member.fieldValues, function (fv_i, fieldValue) {
                fieldValue.id=data.fieldValues[fv_i].id;
                fieldValue.userId=data.fieldValues[fv_i].userId;
                fieldValue.fieldId=data.fieldValues[fv_i].fieldId;
                fieldValue.fieldName=data.fieldValues[fv_i].fieldName;
                fieldValue.value=data.fieldValues[fv_i].value;
            });
            $("#nick"+member.id).text(member.nick);
            alert("Информация обновлена.")
        }
    })

}

function getMember(id) {
    var res = null;
    $.each(CONTACTS,function (g_i, group) {
        $.each(group.members, function (m_i, member) {
            if(member.id==id){
                res = member;
            }
        })
    });
    return res;
}

function resetFile() {
    $("#file").val('');
}

function submitImage() {
    if(!$("#file").val()) return;
    $.ajax( {
        url: "/ContactBook/uploadImage/"+$("#selectedId").val(),
        type: 'POST',
        data: new FormData( $('#file_form') ),
        processData: false,
        contentType: false
    } );
}