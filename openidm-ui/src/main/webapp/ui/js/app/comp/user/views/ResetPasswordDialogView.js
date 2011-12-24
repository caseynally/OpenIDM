/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define(["app/comp/main/controllers/DialogsCtrl"], function(dialogsCtrl) {
	
	
	var obj = {};
	
	obj.dialog = dialogsCtrl;

	obj.getConfirmButton = function() {
		return $("#dialog input[name='dialogOk']");
	};

	obj.getCloseButton = function() {
		return $('#dialogClose');
	}

	obj.getEmailInput = function() {
		return $("#dialog input[name='resetEmail']");
	}

	obj.getPasswordResetLink = function() {
		return $("#passwordResetLink");
	}

	obj.show = function(callback) {
		self = this;

		console.log("showing reset password dialog");
		
		$.ajax({
			type: "GET",
			url: "js/app/comp/user/templates/ResetPasswordTemplate.html",
			dataType: "html",
			success: function(data) {
				self.dialog.setContent(data);
				self.dialog.setActions("<input type='button' name='dialogClose' id='dialogClose' class='button gray floatRight' value='Close' /><input type='button' name='dialogOk' id='dialogOk' class='button gray floatRight' value='Remind me' />");
				self.dialog.setWidth(800);
				self.dialog.setHeight(210);
				self.dialog.show();
				callback();
			},
			error: callback
		});
	};

	obj.showD = function(callback) {
		self = this;

		console.log("showing change password dialog");
		
		$.ajax({
			type: "GET",
			url: "js/app/comp/user/templates/PasswordChangeTemplate.html",
			dataType: "html",
			success: function(data) {
				self.dialog.setContent("<h2>Now you should check email and click special link. This is only demo.</h2>"+data);
				self.dialog.setHeight(240);
				callback();
			},
			error: callback
		});
	};


	obj.close = function() {
		obj.dialog.close();
	}

	obj.enableSaveButton = function() {
		obj.getConfirmButton().removeClass('gray').addClass('orange');
	}

	obj.disableSaveButton = function() {
		obj.getConfirmButton().removeClass('orange').addClass('gray');
	}
	
	obj.showEmail = function() {
	  obj.getFgtnAnswerDiv().hide();
	  obj.getFgtnEmailDiv().show();
	}
	
	obj.showAnswer = function() {
	  obj.getFgtnAnswerDiv().show();
	  obj.getFgtnEmailDiv().hide();
	}
		
	console.log("Reset Password View created");
	return obj;
	
});

