package frontlinesms2

class ContactSearchService {
	static transactional = true
	
	def contactList(params) {
		[contactInstanceList: getContacts(params),
				contactInstanceTotal: countContacts(params),
				contactsSection: Group.get(params.groupId)]
	}
	
	private def getContacts(params) {
		def searchString = getSearchString(params)
		
		if(params.groupId) {
			GroupMembership.searchForContacts(asLong(params.groupId), searchString,
					params.max,
			                params.offset)
		} else if(params.smartGroupId) {
			SmartGroup.getMembersByNameIlike(asLong(params.smartGroupId), searchString, [max:params.max, offset:params.offset])
		} else Contact.findAllByNameIlike(searchString, params)
	}
	
	private def countContacts(params) {
		def searchString = getSearchString(params)
		
		if(params.groupId) {
			GroupMembership.countSearchForContacts(asLong(params.groupId), searchString)
		} else if(params.smartGroupId) {
			SmartGroup.countMembersByNameIlike(asLong(params.smartGroupId), searchString)
		} else Contact.countByNameIlike(searchString)
	}
	
	private def getSearchString(params) {
		params.searchString? "%${params.searchString.toLowerCase()}%": '%'
	}
	
	private def asLong(Long v) { v }
	private def asLong(String s) { s.toLong() }
}
