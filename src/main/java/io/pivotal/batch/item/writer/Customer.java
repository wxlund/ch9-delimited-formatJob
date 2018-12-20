package io.pivotal.batch.item.writer;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_generator")
	@SequenceGenerator(name="customer_generator", sequenceName = "customer_sequence", allocationSize=50)
	@Column(name = "id", updatable = false, nullable = false)

	private long id;
	private String firstName;
	private String middleInitial;
	private String lastName;
	private String address;
	private String city;
	private String state;
	private String zip;
    public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getMiddleInitial() {
			return middleInitial;
		}
		public void setMiddleInitial(String middleInitial) {
			this.middleInitial = middleInitial;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public String getZip() {
			return zip;
		}
		public void setZip(String zip) {
			this.zip = zip;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}

}
