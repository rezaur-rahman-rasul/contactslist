package com.reza.ocl


import grails.web.servlet.mvc.GrailsParameterMap

import javax.servlet.http.HttpServletRequest


class MemberService {

    def save(GrailsParameterMap params) {
        Member member = new Member(params)
        def response = AppUtil.saveResponse(false, member)
        if (member.validate()) {
            member.save(flush: true)
            if (!member.hasErrors()){
                response.isSuccess = true
            }
        }
        return response
    }


    def update(Member member, GrailsParameterMap params) {
        member.properties = params
        def response = AppUtil.saveResponse(false, member)
        if (member.validate()) {
            member.save(flush: true)
            if (!member.hasErrors()){
                response.isSuccess = true
            }
        }
        return response
    }


    def getById(Serializable id) {
        return Member.get(id)
    }


    def list(GrailsParameterMap params) {
        params.max = params.max ?: GlobalConfig.itemsPerPage()
        List<Member> memberList = Member.createCriteria().list(params) {
            if (params?.colName && params?.colValue) {
                like(params.colName, "%" + params.colValue + "%")
            }
            if (!params.sort) {
                order("id", "desc")
            }
        }
        return [list: memberList, count: memberList.totalCount]
    }


    def delete(Member member) {
        try {
            member.delete(flush: true)
        } catch (Exception e) {
            println(e.getMessage())
            return false
        }
        return true
    }

    static class ContactService {

        AuthenticationService authenticationService
        ContactDetailsService contactDetailsService

        def save(GrailsParameterMap params, HttpServletRequest request) {
            Contact contact = new Contact(params)
            contact.member = authenticationService.getMember()
            def response = AppUtil.saveResponse(false, contact)
            if (contact.validate()) {
                contact.save(flush: true)
                if (!contact.hasErrors()){
                    response.isSuccess = true
                    contactDetailsService.createOrUpdateDetails(contact, params)
                    uploadImage(contact, request)
                }
            }
            return response
        }

        def update(Contact contact, GrailsParameterMap params, HttpServletRequest request) {
            contact.properties = params
            def response = AppUtil.saveResponse(false, contact)
            if (contact.validate()) {
                contact.save(flush: true)
                if (!contact.hasErrors()){
                    response.isSuccess = true
                    contactDetailsService.createOrUpdateDetails(contact, params)
                    uploadImage(contact, request)
                }
            }
            return response
        }


        def get(Serializable id) {
            return Contact.get(id)
        }


        def list(GrailsParameterMap params) {
            params.max = params.max ?: GlobalConfig.itemsPerPage()
            List<Contact> contactList = Contact.createCriteria().list(params) {
                if (params?.colName && params?.colValue) {
                    like(params.colName, "%" + params.colValue + "%")
                }
                if (!params.sort) {
                    order("id", "desc")
                }
                eq("member", authenticationService.getMember())
            }
            return [list: contactList, count: contactList.totalCount]
        }


        def delete(Contact contact) {
            try {
                contact.delete(flush: true)
            } catch (Exception e) {
                println(e.getMessage())
                return false
            }
            return true
        }


        def uploadImage(Contact contact, HttpServletRequest request){
            if (request.getFile("contactImage") && !request.getFile("contactImage").filename.equals("")){
                String image = FileUtil.uploadContactImage(contact.id, request.getFile("contactImage"))
                if (!image.equals("")){
                    contact.image = image
                    contact.save(flush:true)
                }
            }
        }

    }
}