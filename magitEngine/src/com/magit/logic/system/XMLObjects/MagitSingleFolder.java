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
 *       &lt;all>
 *         &lt;element ref="{}name" minOccurs="0"/>
 *         &lt;element ref="{}last-updater"/>
 *         &lt;element ref="{}last-update-date"/>
 *         &lt;element name="items">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{}item" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *       &lt;attribute name="is-root" default="false">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}boolean">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "MagitSingleFolder")
public class MagitSingleFolder {

    protected String name;
    @XmlElement(name = "last-updater", required = true)
    protected String lastUpdater;
    @XmlElement(name = "last-update-date", required = true)
    protected String lastUpdateDate;
    @XmlElement(required = true)
    protected MagitSingleFolder.Items items;
    @XmlAttribute(name = "is-root")
    protected Boolean isRoot;
    @XmlAttribute(name = "id", required = true)
    protected String id;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the lastUpdater property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLastUpdater() {
        return lastUpdater;
    }

    /**
     * Sets the value of the lastUpdater property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLastUpdater(String value) {
        this.lastUpdater = value;
    }

    /**
     * Gets the value of the lastUpdateDate property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * Sets the value of the lastUpdateDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLastUpdateDate(String value) {
        this.lastUpdateDate = value;
    }

    /**
     * Gets the value of the items property.
     *
     * @return possible object is
     * {@link MagitSingleFolder.Items }
     */
    public MagitSingleFolder.Items getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     *
     * @param value allowed object is
     *              {@link MagitSingleFolder.Items }
     */
    public void setItems(MagitSingleFolder.Items value) {
        this.items = value;
    }

    /**
     * Gets the value of the isRoot property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public boolean isIsRoot() {
        if (isRoot == null) {
            return false;
        } else {
            return isRoot;
        }
    }

    /**
     * Sets the value of the isRoot property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIsRoot(Boolean value) {
        this.isRoot = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }


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
     *         &lt;element ref="{}item" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "item"
    })
    public static class Items {

        @XmlElement(required = true)
        protected List<Item> item;

        /**
         * Gets the value of the item property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the item property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getItem().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Item }
         */
        public List<Item> getItem() {
            if (item == null) {
                item = new ArrayList<Item>();
            }
            return this.item;
        }

    }

}
