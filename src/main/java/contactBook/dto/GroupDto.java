package contactBook.dto;

import contactBook.model.Group;
import contactBook.model.User;

import java.util.HashSet;
import java.util.Set;

public class GroupDto {
    private int groupId;
    private String groupName;
    private Set<UserDto> members = new HashSet<>();

    public GroupDto() {}

    public GroupDto(Group group){
        this.groupId = group.getId();
        this.groupName = group.getName();
        for (User user : group.getMembers()){
            members.add(new UserDto(user));
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<UserDto> getMembers() {
        return members;
    }

    public void setMembers(Set<UserDto> members) {
        this.members = members;
    }
}
