package frontlinesms2

class FsmsTagLib {
	static namespace = 'fsms'
	def expressionProcessorService

	def render = { att ->
		out << g.render(att)
	}

	def i18n = { att ->
		r.script(disposition:'head') {
			att.keys.tokenize(',')*.trim().each {
				def propVal = g.message(code:it)
				propVal = propVal.replaceAll("\\'", "\\\\'")
				out << "\ti18nStrings['$it'] = '${propVal}';\n"
			}
		}
	}
	
	def confirmTable = { att ->
		out << '<table id="' + (att.instanceClass.simpleName.toLowerCase() - 'fconnection') + '-confirm" class="connection-confirm-table">'
		out << confirmTypeRow(att)
		def fields = getFields(att)
		if(fields instanceof Map) {
			generateConfirmSection(att, fields)
		} else {
			fields.each { out << confirmTableRow(att + [field:it.trim()]) }
		}
		out << '</table>'
	}
	
	def confirmTypeRow = { att ->
		out << '<tr>'
		out << '<td class="field-label">'
		out << g.message(code:"${att.instanceClass.simpleName.toLowerCase()}.type.label")
		out << '</td>'
		out << '<td id="confirm-type"></td>'
		out << '</tr>'
	}
	
	def confirmTableRow = { att ->
		out << '<tr>'
		out << '<td class="field-label">'
		out << getFieldLabel(att.instanceClass, att.field)
		out << '</td>'
		out << '<td id="confirm-' + att.field + '"></td>'
		out << '</tr>'
	}
	
	def inputs = { att ->
		println "att is $att"
		def fields = getFields(att)
		if(fields instanceof Map) {
			generateSection(att, fields)
			
		} else {
			fields.each {
				out << input(att + [field:it])
			}
		}
		
	}
	
	def input = { att ->
		def groovyKey = att.field
		def htmlKey = (att.fieldPrefix?:att.instanceClass?att.instanceClass.shortName:'') + att.field
		def val = att.instance?."$groovyKey"
		def instanceClass = att.instance?.getClass()?: att.instanceClass
		
		['instance', 'instanceClass'].each { att.remove(it) }
		att += [name:htmlKey, value:val]
		out << '<div class="field">'
		out << '<label for="' + htmlKey + '">'
		out << '' + getFieldLabel(instanceClass, groovyKey)
		out << '</label>'
		
		if(att.password || isPassword(instanceClass, groovyKey)) {	
			out << g.passwordField(att)
		} else if(instanceClass.metaClass.hasProperty(null, groovyKey)?.type.enum) {
			out << g.select(att + [from:instanceClass.metaClass.hasProperty(null, groovyKey).type.values(),
						noSelection:[null:'- Select -']])
		} else if(isBooleanField(instanceClass, groovyKey)) {
			out << g.checkBox(att)
		} else out << g.textField(att)
		
		out << '</div>'
	}

	def magicWand = { att ->
		def currentView = att.view
		def target = att.target
		def fields = expressionProcessorService.findByView(currentView)
		out << '<div id="magic-wand">'
		out << '<select class="dropdown extra-msg-btn" name="magicwand" id="magicwand" onchange="insertDynamicField(\'messageText\', \'magicwand\')" aria-disabled="false">'
		out << '<option id="magicwand-na" value="na" class="na">I am a magic wand!!!</option>'
		fields.each {
			out << '<option class="folder" value="'+it.key+'" ' + (it.value?'':'disabled="disabled" ') + '>' + g.message(code:"dynamicfield.${it.key}.label") + '</option>'
		}
		out << '</select>'
		out << '</div>'
	}
	
	private def getFields(att) {
		def fields = att.remove('fields')
		if(!fields) fields = att.instanceClass?.configFields
		if(fields instanceof String) fields = fields.tokenize(',')*.trim()
		return fields
	}
	
	private def getFieldLabel(clazz, fieldName) {
		g.message(code:"${clazz.simpleName.toLowerCase()}.${fieldName}.label")
	}
	
	private def isPassword(instanceClass, groovyKey) {
		return instanceClass.metaClass.hasProperty(null, 'passwords') &&
				groovyKey in instanceClass.passwords
	}
	
	private def isBooleanField(instanceClass, groovyKey) {
		return instanceClass.metaClass.hasProperty(null, groovyKey).type in [Boolean, boolean]
	}
	
	private def generateSection(att, fields) {
		def keys = fields.keySet()
		keys.each { key ->
			if(fields[key]) {
				out << "<div id=\"$key-subsection\">"
				out << "<fieldset>"
				out << "<legend>"
				out << input(att + [field:key])
				out << "</legend>"
				
				//handle subsections within a subsection
				if(fields[key] instanceof LinkedHashMap) {
					generateSection(att, fields[key])
				} else {
					fields[key].each {field ->
						if(field instanceof String) {
							 out << input(att + [field:field] + [class:"$key-subsection-member"])
						}
					}
				}
				out << "</fieldset>"
				out << "</div>"
			} else {
				out << input(att + [field:key])
			}
			
		}
	}
	
	private def generateConfirmSection(att, fields) {
		def keys = fields.keySet()
		keys.each { key ->
			if(fields[key]) {
				out << "<div class=\"confirm-$key-subsection\">"
				out << confirmTableRow(att + [field:key])
				
				//handle subsections within a subsection
				if(fields[key] instanceof LinkedHashMap) {
					generateConfirmSection(att, fields[key])
				} else {
					fields[key].each {field ->
						if(field instanceof String) {
							 out << confirmTableRow(att + [field:field] + [class:"subsection-member"])
						}
					}
				}
				out << "</div>"
			} else {
				out << confirmTableRow(att + [field:key])
			}
			
		}
	}
}
