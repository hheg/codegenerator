package codegen;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString2;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy2;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestObject", propOrder = {"startDate","endDate"})
@codegen.ValidTestObject
@codegen.IdFields({ "startDate", "endDate" })
public class TestObject implements ToString2
{

    @XmlElement(name = "StartDate", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    @javax.validation.constraints.NotNull
    protected LocalDate startDate;
    @XmlElement(name = "EndDate", required = true, type = String.class, nillable = true)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate endDate;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate value) {
        this.startDate = value;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate value) {
        this.endDate = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final TestObject that = ((TestObject) object);
        {
            LocalDate leftStartDate;
            leftStartDate = this.getStartDate();
            LocalDate rightStartDate;
            rightStartDate = that.getStartDate();
            if (leftStartDate!= null) {
                if (rightStartDate!= null) {
                    if (!leftStartDate.equals(rightStartDate)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (rightStartDate!= null) {
                    return false;
                }
            }
        }
        {
            LocalDate leftEndDate;
            leftEndDate = this.getEndDate();
            LocalDate rightEndDate;
            rightEndDate = that.getEndDate();
            if (leftEndDate!= null) {
                if (rightEndDate!= null) {
                    if (!leftEndDate.equals(rightEndDate)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (rightEndDate!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            LocalDate theStartDate;
            theStartDate = this.getStartDate();
            if (theStartDate!= null) {
                currentHashCode += theStartDate.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            LocalDate theEndDate;
            theEndDate = this.getEndDate();
            if (theEndDate!= null) {
                currentHashCode += theEndDate.hashCode();
            }
        }
        return currentHashCode;
    }

    public String toString() {
        final ToStringStrategy2 strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy2 strategy) {
        {
            LocalDate theStartDate;
            theStartDate = this.getStartDate();
            strategy.appendField(locator, this, "startDate", buffer, theStartDate, (this.startDate!= null));
        }
        {
            LocalDate theEndDate;
            theEndDate = this.getEndDate();
            strategy.appendField(locator, this, "endDate", buffer, theEndDate, (this.endDate!= null));
        }
        return buffer;
    }

}
