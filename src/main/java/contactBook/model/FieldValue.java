package contactBook.model;

import javax.persistence.*;

@Entity
@Table(name = "values")
public class FieldValue extends BaseModel{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field", nullable = false)
    private Field field;

    @Column(name = "_value")
    private String value;

    @Column(name = "rank")
    private int rank;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldValue)) return false;

        FieldValue that = (FieldValue) o;

        if (!user.equals(that.user)) return false;
        if (!field.equals(that.field)) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + field.hashCode();
        return result;
    }
}
