package com.example.buildpro.service;

import com.example.buildpro.model.Address;
import com.example.buildpro.model.User;
import com.example.buildpro.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AddressService {
    
    @Autowired
    private AddressRepository addressRepository;
    
    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUser(user);
    }
    
    public Address createAddress(User user, Address address) {
        address.setUser(user);
        
        // If this is the first address, set it as default
        if (addressRepository.findByUser(user).isEmpty()) {
            address.setIsDefault(true);
        }
        
        return addressRepository.save(address);
    }
    
    public Address updateAddress(Long addressId, User user, Address addressDetails) {
        Optional<Address> addressOpt = addressRepository.findByIdAndUser(addressId, user);
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            address.setAddressLine1(addressDetails.getAddressLine1());
            address.setAddressLine2(addressDetails.getAddressLine2());
            address.setCity(addressDetails.getCity());
            address.setState(addressDetails.getState());
            address.setPostalCode(addressDetails.getPostalCode());
            address.setCountry(addressDetails.getCountry());
            address.setIsDefault(addressDetails.getIsDefault());
            
            // If setting as default, remove default from other addresses
            if (address.getIsDefault()) {
                setAsDefaultAddress(address, user);
            }
            
            return addressRepository.save(address);
        }
        return null;
    }
    
    public boolean deleteAddress(Long addressId, User user) {
        Optional<Address> addressOpt = addressRepository.findByIdAndUser(addressId, user);
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            
            // If deleting default address, set another address as default
            if (address.getIsDefault()) {
                List<Address> otherAddresses = addressRepository.findByUser(user);
                otherAddresses.remove(address);
                if (!otherAddresses.isEmpty()) {
                    otherAddresses.get(0).setIsDefault(true);
                    addressRepository.save(otherAddresses.get(0));
                }
            }
            
            addressRepository.delete(address);
            return true;
        }
        return false;
    }
    
    public Address getDefaultAddress(User user) {
        List<Address> defaultAddresses = addressRepository.findByUserAndIsDefaultTrue(user);
        return defaultAddresses.isEmpty() ? null : defaultAddresses.get(0);
    }
    
    private void setAsDefaultAddress(Address newDefault, User user) {
        List<Address> userAddresses = addressRepository.findByUser(user);
        for (Address address : userAddresses) {
            if (!address.getId().equals(newDefault.getId()) && address.getIsDefault()) {
                address.setIsDefault(false);
                addressRepository.save(address);
            }
        }
    }
}
