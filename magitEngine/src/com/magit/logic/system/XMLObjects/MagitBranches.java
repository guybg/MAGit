//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.29 at 07:58:25 PM IDT 
//


package com.magit.logic.system.XMLObjects;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}head"/>
 *         &lt;element ref="{}MagitSingleBranch" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "head",
        "magitSingleBranch"
})
@XmlRootElement(name = "MagitBranches")
public class MagitBranches {

    @XmlElement(required = true)
    protected String head;
    @XmlElement(name = "MagitSingleBranch", required = true)
    protected List<MagitSingleBranch> magitSingleBranch;

    /**
     * Gets the value of the head property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getHead() {
        return head;
    }

    /**
     * Sets the value of the head property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHead(String value) {
        this.head = value;
    }

    /**
     * Gets the value of the magitSingleBranch property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the magitSingleBranch property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMagitSingleBranch().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MagitSingleBranch }
     */
    public List<MagitSingleBranch> getMagitSingleBranch() {
        if (magitSingleBranch == null) {
            magitSingleBranch = new ArrayList<MagitSingleBranch>();
        }
        return this.magitSingleBranch;
    }

}
