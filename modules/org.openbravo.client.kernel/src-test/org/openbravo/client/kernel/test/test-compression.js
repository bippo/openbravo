OB.Layout.ViewManager.loadedWindowClassName = '_143';
isc.ClassFactory.defineClass('_143', isc.OBStandardWindow).addProperties({
  windowId: '143',
  multiDocumentEnabled: false,
  viewProperties: {
    windowId: '143',
    tabTitle: 'Header',
    entity: 'Order',
    tabId: '186',
    moduleId: '0',
    defaultEditMode: false,
    mapping250: '/SalesOrder/Header',
    isAcctTab: false,
    isTrlTab: false,
    standardProperties: {
      inpTabId: '186',
      inpwindowId: '143',
      inpTableId: '259',
      inpkeyColumnId: 'C_Order_ID',
      inpKeyName: 'inpcOrderId'
    },
    propertyToColumns: [{
      property: 'organization',
      inpColumn: 'inpadOrgId',
      dbColumn: 'AD_Org_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'transactionDocument',
      inpColumn: 'inpcDoctypetargetId',
      dbColumn: 'C_DocTypeTarget_ID',
      sessionProperty: false,
      type: '_id_22F546D49D3A48E1B2B4F50446A8DE58'
    }, {
      property: 'documentNo',
      inpColumn: 'inpdocumentno',
      dbColumn: 'DocumentNo',
      sessionProperty: false,
      type: '_id_10'
    }, {
      property: 'orderDate',
      inpColumn: 'inpdateordered',
      dbColumn: 'DateOrdered',
      sessionProperty: true,
      type: '_id_15'
    }, {
      property: 'businessPartner',
      inpColumn: 'inpcBpartnerId',
      dbColumn: 'C_BPartner_ID',
      sessionProperty: true,
      type: '_id_800057'
    }, {
      property: 'partnerAddress',
      inpColumn: 'inpcBpartnerLocationId',
      dbColumn: 'C_BPartner_Location_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'priceList',
      inpColumn: 'inpmPricelistId',
      dbColumn: 'M_PriceList_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'scheduledDeliveryDate',
      inpColumn: 'inpdatepromised',
      dbColumn: 'DatePromised',
      sessionProperty: true,
      type: '_id_15'
    }, {
      property: 'paymentMethod',
      inpColumn: 'inpfinPaymentmethodId',
      dbColumn: 'FIN_Paymentmethod_ID',
      sessionProperty: false,
      type: '_id_19'
    }, {
      property: 'paymentTerms',
      inpColumn: 'inpcPaymenttermId',
      dbColumn: 'C_PaymentTerm_ID',
      sessionProperty: false,
      type: '_id_19'
    }, {
      property: 'warehouse',
      inpColumn: 'inpmWarehouseId',
      dbColumn: 'M_Warehouse_ID',
      sessionProperty: true,
      type: '_id_197'
    }, {
      property: 'invoiceTerms',
      inpColumn: 'inpinvoicerule',
      dbColumn: 'InvoiceRule',
      sessionProperty: false,
      type: '_id_150'
    }, {
      property: 'documentStatus',
      inpColumn: 'inpdocstatus',
      dbColumn: 'DocStatus',
      sessionProperty: false,
      type: '_id_FF80818130217A350130218D802B0011'
    }, {
      property: 'grandTotalAmount',
      inpColumn: 'inpgrandtotal',
      dbColumn: 'GrandTotal',
      sessionProperty: false,
      type: '_id_12'
    }, {
      property: 'summedLineAmount',
      inpColumn: 'inptotallines',
      dbColumn: 'TotalLines',
      sessionProperty: false,
      type: '_id_12'
    }, {
      property: 'currency',
      inpColumn: 'inpcCurrencyId',
      dbColumn: 'C_Currency_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'userContact',
      inpColumn: 'inpadUserId',
      dbColumn: 'AD_User_ID',
      sessionProperty: false,
      type: '_id_19'
    }, {
      property: 'project',
      inpColumn: 'inpcProjectId',
      dbColumn: 'C_Project_ID',
      sessionProperty: false,
      type: '_id_800061'
    }, {
      property: 'documentType',
      inpColumn: 'inpcDoctypeId',
      dbColumn: 'C_DocType_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'orderReference',
      inpColumn: 'inpporeference',
      dbColumn: 'POReference',
      sessionProperty: false,
      type: '_id_10'
    }, {
      property: 'salesRepresentative',
      inpColumn: 'inpsalesrepId',
      dbColumn: 'SalesRep_ID',
      sessionProperty: false,
      type: '_id_190'
    }, {
      property: 'description',
      inpColumn: 'inpdescription',
      dbColumn: 'Description',
      sessionProperty: false,
      type: '_id_14'
    }, {
      property: 'invoiceAddress',
      inpColumn: 'inpbilltoId',
      dbColumn: 'BillTo_ID',
      sessionProperty: false,
      type: '_id_159'
    }, {
      property: 'deliveryLocation',
      inpColumn: 'inpdeliveryLocationId',
      dbColumn: 'Delivery_Location_ID',
      sessionProperty: false,
      type: '_id_159'
    }, {
      property: 'copyFrom',
      inpColumn: 'inpcopyfrom',
      dbColumn: 'CopyFrom',
      sessionProperty: false,
      type: '_id_28'
    }, {
      property: 'copyFromPO',
      inpColumn: 'inpcopyfrompo',
      dbColumn: 'CopyFromPO',
      sessionProperty: false,
      type: '_id_28'
    }, {
      property: 'documentAction',
      inpColumn: 'inpdocaction',
      dbColumn: 'DocAction',
      sessionProperty: false,
      type: '_id_FF80818130217A35013021A672400035'
    }, {
      property: 'deliveryMethod',
      inpColumn: 'inpdeliveryviarule',
      dbColumn: 'DeliveryViaRule',
      sessionProperty: true,
      type: '_id_152'
    }, {
      property: 'shippingCompany',
      inpColumn: 'inpmShipperId',
      dbColumn: 'M_Shipper_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'deliveryTerms',
      inpColumn: 'inpdeliveryrule',
      dbColumn: 'DeliveryRule',
      sessionProperty: false,
      type: '_id_151'
    }, {
      property: 'freightCostRule',
      inpColumn: 'inpfreightcostrule',
      dbColumn: 'FreightCostRule',
      sessionProperty: true,
      type: '_id_153'
    }, {
      property: 'freightAmount',
      inpColumn: 'inpfreightamt',
      dbColumn: 'FreightAmt',
      sessionProperty: false,
      type: '_id_12'
    }, {
      property: 'printDiscount',
      inpColumn: 'inpisdiscountprinted',
      dbColumn: 'IsDiscountPrinted',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'priority',
      inpColumn: 'inppriorityrule',
      dbColumn: 'PriorityRule',
      sessionProperty: false,
      type: '_id_154'
    }, {
      property: 'salesCampaign',
      inpColumn: 'inpcCampaignId',
      dbColumn: 'C_Campaign_ID',
      sessionProperty: false,
      type: '_id_19'
    }, {
      property: 'chargeAmount',
      inpColumn: 'inpchargeamt',
      dbColumn: 'ChargeAmt',
      sessionProperty: false,
      type: '_id_12'
    }, {
      property: 'charge',
      inpColumn: 'inpcChargeId',
      dbColumn: 'C_Charge_ID',
      sessionProperty: false,
      type: '_id_200'
    }, {
      property: 'activity',
      inpColumn: 'inpcActivityId',
      dbColumn: 'C_Activity_ID',
      sessionProperty: false,
      type: '_id_19'
    }, {
      property: 'trxOrganization',
      inpColumn: 'inpadOrgtrxId',
      dbColumn: 'AD_OrgTrx_ID',
      sessionProperty: false,
      type: '_id_130'
    }, {
      property: 'ndDimension',
      inpColumn: 'inpuser2Id',
      dbColumn: 'User2_ID',
      sessionProperty: false,
      type: '_id_10'
    }, {
      property: 'stDimension',
      inpColumn: 'inpuser1Id',
      dbColumn: 'User1_ID',
      sessionProperty: false,
      type: '_id_10'
    }, {
      property: 'deliveryNotes',
      inpColumn: 'inpdeliverynotes',
      dbColumn: 'Deliverynotes',
      sessionProperty: false,
      type: '_id_14'
    }, {
      property: 'incoterms',
      inpColumn: 'inpcIncotermsId',
      dbColumn: 'C_Incoterms_ID',
      sessionProperty: false,
      type: '_id_19'
    }, {
      property: 'iNCOTERMSDescription',
      inpColumn: 'inpincotermsdescription',
      dbColumn: 'Incotermsdescription',
      sessionProperty: false,
      type: '_id_14'
    }, {
      property: 'generateTemplate',
      inpColumn: 'inpgeneratetemplate',
      dbColumn: 'Generatetemplate',
      sessionProperty: false,
      type: '_id_28'
    }, {
      property: 'selfService',
      inpColumn: 'inpisselfservice',
      dbColumn: 'IsSelfService',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'dropShipLocation',
      inpColumn: 'inpdropshipLocationId',
      dbColumn: 'DropShip_Location_ID',
      sessionProperty: false,
      type: '_id_159'
    }, {
      property: 'dropShipPartner',
      inpColumn: 'inpdropshipBpartnerId',
      dbColumn: 'DropShip_BPartner_ID',
      sessionProperty: false,
      type: '_id_173'
    }, {
      property: 'dropShipContact',
      inpColumn: 'inpdropshipUserId',
      dbColumn: 'DropShip_User_ID',
      sessionProperty: false,
      type: '_id_110'
    }, {
      property: 'selected',
      inpColumn: 'inpisselected',
      dbColumn: 'IsSelected',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'posted',
      inpColumn: 'inpposted',
      dbColumn: 'Posted',
      sessionProperty: false,
      type: '_id_234'
    }, {
      property: 'priceIncludesTax',
      inpColumn: 'inpistaxincluded',
      dbColumn: 'IsTaxIncluded',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'formOfPayment',
      inpColumn: 'inppaymentrule',
      dbColumn: 'PaymentRule',
      sessionProperty: false,
      type: '_id_195'
    }, {
      property: 'salesTransaction',
      inpColumn: 'inpissotrx',
      dbColumn: 'IsSOTrx',
      sessionProperty: true,
      type: '_id_20'
    }, {
      property: 'datePrinted',
      inpColumn: 'inpdateprinted',
      dbColumn: 'DatePrinted',
      sessionProperty: false,
      type: '_id_15'
    }, {
      property: 'processed',
      inpColumn: 'inpprocessed',
      dbColumn: 'Processed',
      sessionProperty: true,
      type: '_id_20'
    }, {
      property: 'processNow',
      inpColumn: 'inpprocessing',
      dbColumn: 'Processing',
      sessionProperty: false,
      type: '_id_28'
    }, {
      property: 'accountingDate',
      inpColumn: 'inpdateacct',
      dbColumn: 'DateAcct',
      sessionProperty: false,
      type: '_id_15'
    }, {
      property: 'print',
      inpColumn: 'inpisprinted',
      dbColumn: 'IsPrinted',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'reinvoice',
      inpColumn: 'inpisinvoiced',
      dbColumn: 'IsInvoiced',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'delivered',
      inpColumn: 'inpisdelivered',
      dbColumn: 'IsDelivered',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'active',
      inpColumn: 'inpisactive',
      dbColumn: 'IsActive',
      sessionProperty: false,
      type: '_id_20'
    }, {
      property: 'client',
      inpColumn: 'inpadClientId',
      dbColumn: 'AD_Client_ID',
      sessionProperty: true,
      type: '_id_19'
    }, {
      property: 'id',
      inpColumn: 'inpcOrderId',
      dbColumn: 'C_Order_ID',
      sessionProperty: true,
      type: '_id_13'
    }, {
      property: 'id',
      inpColumn: 'C_Order_ID',
      dbColumn: 'C_Order_ID',
      sessionProperty: true,
      type: '_id_13'
    }],
    actionToolbarButtons: [{
      id: '6560',
      title: 'Copy Lines',
      obManualURL: '/ad_actionButton/CopyFromOrder.html',
      command: 'DEFAULT',
      property: 'copyFrom',
      autosave: true,
      processId: '211',
      labelValue: {},
      displayIf: function (item, value, form, currentValues) {
        currentValues = currentValues || form.view.getCurrentValues();
        OB.Utilities.fixNull250(currentValues);
        var context = form.view.getContextInfo(false, true, true);
        return context && (currentValues.processed === false);
      }
    }, {
      id: '804079',
      title: 'Copy from Order',
      obManualURL: '/ad_actionButton/CopyFromPOOrder.html',
      command: 'DEFAULT',
      property: 'copyFromPO',
      autosave: true,
      processId: '800165',
      labelValue: {},
      displayIf: function (item, value, form, currentValues) {
        currentValues = currentValues || form.view.getCurrentValues();
        OB.Utilities.fixNull250(currentValues);
        var context = form.view.getContextInfo(false, true, true);
        return context && (currentValues.processed === false);
      }
    }, {
      id: '1083',
      title: 'Process Order',
      obManualURL: '/SalesOrder/Header_Edition.html',
      command: 'BUTTONDocAction104',
      property: 'documentAction',
      autosave: true,
      processId: '104',
      labelValue: {
        'PR': 'Process',
        'TR': 'Transfer',
        'CO': 'Book',
        'PO': 'Post',
        'AP': 'Approve',
        'XL': 'Unlock',
        '--': '<None>',
        'RC': 'Void',
        'RJ': 'Reject',
        'RE': 'Reactivate',
        'VO': 'Void',
        'RA': 'Reverse - Accrual',
        'CL': 'Close'
      },
      displayIf: function (item, value, form, currentValues) {
        currentValues = currentValues || form.view.getCurrentValues();
        OB.Utilities.fixNull250(currentValues);
        var context = form.view.getContextInfo(false, true, true);
        return context && (currentValues.documentStatus !== 'VO' && currentValues.documentStatus !== 'CL');
      }
    }],
    showParentButtons: true,
    buttonsHaveSessionLogic: false,
    iconToolbarButtons: [{
      action: function () {
        OB.ToolbarUtils.print(this.view, '../orders/print.html', false);
      },
      buttonType: 'print',
      prompt: 'Print Record'
    }, {
      action: function () {
        OB.ToolbarUtils.print(this.view, '../orders/send.html', false);
      },
      buttonType: 'email',
      prompt: 'Email'
    }],
    hasChildTabs: true,
    initWidget: function () {
      this.dataSource = OB.Datasource.create({
        createClassName: 'OBViewDataSource',
        titleField: OB.Constants.IDENTIFIER,
        dataURL: '/openbravo/org.openbravo.service.datasource/Order',
        recordXPath: '/response/data',
        dataFormat: 'json',
        operationBindings: [{
          operationType: 'fetch',
          dataProtocol: 'postParams',
          requestProperties: {
            httpMethod: 'POST'
          }
        }, {
          operationType: 'add',
          dataProtocol: 'postMessage'
        }, {
          operationType: 'remove',
          dataProtocol: 'postParams',
          requestProperties: {
            httpMethod: 'DELETE'
          }
        }, {
          operationType: 'update',
          dataProtocol: 'postMessage',
          requestProperties: {
            httpMethod: 'PUT'
          }
        }],
        requestProperties: {
          params: {
            _className: 'OBViewDataSource'
          }
        },
        fields: [{
          name: 'id',
          type: '_id_13',
          additional: false,
          hidden: true,
          primaryKey: true,
          canSave: false,
          title: 'id'
        }, {
          name: 'client',
          type: '_id_19',
          additional: false,
          canSave: false,
          title: 'client',
          hidden: true
        }, {
          name: 'client._identifier',
          type: 'text',
          hidden: true,
          canSave: false,
          title: 'client'
        }, {
          name: 'organization',
          type: '_id_19',
          additional: false,
          required: true,
          title: 'organization',
          hidden: true
        }, {
          name: 'organization._identifier',
          type: 'text',
          hidden: true,
          title: 'organization'
        }, {
          name: 'active',
          type: '_id_20',
          additional: false,
          title: 'active'
        }, {
          name: 'creationDate',
          type: '_id_16',
          additional: false,
          canSave: false,
          title: 'creationDate'
        }, {
          name: 'createdBy',
          type: '_id_30',
          additional: false,
          canSave: false,
          title: 'createdBy',
          hidden: true
        }, {
          name: 'createdBy._identifier',
          type: 'text',
          hidden: true,
          canSave: false,
          title: 'createdBy'
        }, {
          name: 'updated',
          type: '_id_16',
          additional: false,
          canSave: false,
          title: 'updated'
        }, {
          name: 'updatedBy',
          type: '_id_30',
          additional: false,
          canSave: false,
          title: 'updatedBy',
          hidden: true
        }, {
          name: 'updatedBy._identifier',
          type: 'text',
          hidden: true,
          canSave: false,
          title: 'updatedBy'
        }, {
          name: 'salesTransaction',
          type: '_id_20',
          additional: false,
          title: 'salesTransaction'
        }, {
          name: 'documentNo',
          type: '_id_10',
          additional: false,
          required: true,
          length: 30,
          title: 'documentNo'
        }, {
          name: 'documentStatus',
          type: '_id_FF80818130217A350130218D802B0011',
          additional: false,
          canSave: false,
          length: 60,
          title: 'documentStatus',
          valueMap: {
            'CO': 'Booked',
            'CL': 'Closed',
            'DR': 'Draft',
            'NA': 'Not Accepted',
            'WP': 'Not Paid',
            'RE': 'Re-Opened',
            'IP': 'Under Way',
            '??': 'Unknown',
            'VO': 'Voided'
          }
        }, {
          name: 'documentAction',
          type: '_id_FF80818130217A35013021A672400035',
          additional: false,
          required: true,
          length: 60,
          title: 'documentAction',
          valueMap: {
            '--': '<None>',
            'AP': 'Approve',
            'CO': 'Book',
            'CL': 'Close',
            'PO': 'Post',
            'PR': 'Process',
            'RE': 'Reactivate',
            'RJ': 'Reject',
            'RA': 'Reverse - Accrual',
            'XL': 'Unlock',
            'RC': 'Void',
            'VO': 'Void'
          }
        }, {
          name: 'processNow',
          type: '_id_28',
          additional: false,
          title: 'processNow'
        }, {
          name: 'processed',
          type: '_id_20',
          additional: false,
          canSave: false,
          title: 'processed'
        }, {
          name: 'documentType',
          type: '_id_19',
          additional: false,
          canSave: false,
          title: 'documentType',
          hidden: true
        }, {
          name: 'documentType._identifier',
          type: 'text',
          hidden: true,
          canSave: false,
          title: 'documentType'
        }, {
          name: 'transactionDocument',
          type: '_id_22F546D49D3A48E1B2B4F50446A8DE58',
          additional: false,
          required: true,
          title: 'transactionDocument',
          hidden: true
        }, {
          name: 'transactionDocument._identifier',
          type: 'text',
          hidden: true,
          title: 'transactionDocument'
        }, {
          name: 'description',
          type: '_id_14',
          additional: false,
          length: 255,
          title: 'description'
        }, {
          name: 'delivered',
          type: '_id_20',
          additional: false,
          canSave: false,
          title: 'delivered'
        }, {
          name: 'reinvoice',
          type: '_id_20',
          additional: false,
          canSave: false,
          title: 'reinvoice'
        }, {
          name: 'print',
          type: '_id_20',
          additional: false,
          canSave: false,
          title: 'print'
        }, {
          name: 'selected',
          type: '_id_20',
          additional: false,
          title: 'selected'
        }, {
          name: 'salesRepresentative',
          type: '_id_190',
          additional: false,
          title: 'salesRepresentative',
          hidden: true
        }, {
          name: 'salesRepresentative._identifier',
          type: 'text',
          hidden: true,
          title: 'salesRepresentative'
        }, {
          name: 'orderDate',
          type: '_id_15',
          additional: false,
          required: true,
          title: 'orderDate'
        }, {
          name: 'scheduledDeliveryDate',
          type: '_id_15',
          additional: false,
          title: 'scheduledDeliveryDate'
        }, {
          name: 'datePrinted',
          type: '_id_15',
          additional: false,
          title: 'datePrinted'
        }, {
          name: 'accountingDate',
          type: '_id_15',
          additional: false,
          required: true,
          title: 'accountingDate'
        }, {
          name: 'businessPartner',
          type: '_id_800057',
          additional: false,
          required: true,
          title: 'businessPartner',
          hidden: true
        }, {
          name: 'businessPartner._identifier',
          type: 'text',
          hidden: true,
          title: 'businessPartner'
        }, {
          name: 'invoiceAddress',
          type: '_id_159',
          additional: false,
          title: 'invoiceAddress',
          hidden: true
        }, {
          name: 'invoiceAddress._identifier',
          type: 'text',
          hidden: true,
          title: 'invoiceAddress'
        }, {
          name: 'partnerAddress',
          type: '_id_19',
          additional: false,
          required: true,
          title: 'partnerAddress',
          hidden: true
        }, {
          name: 'partnerAddress._identifier',
          type: 'text',
          hidden: true,
          title: 'partnerAddress'
        }, {
          name: 'orderReference',
          type: '_id_10',
          additional: false,
          length: 20,
          title: 'orderReference'
        }, {
          name: 'printDiscount',
          type: '_id_20',
          additional: false,
          title: 'printDiscount'
        }, {
          name: 'currency',
          type: '_id_19',
          additional: false,
          canSave: false,
          title: 'currency',
          hidden: true
        }, {
          name: 'currency._identifier',
          type: 'text',
          hidden: true,
          canSave: false,
          title: 'currency'
        }, {
          name: 'formOfPayment',
          type: '_id_195',
          additional: false,
          required: true,
          length: 60,
          title: 'formOfPayment',
          valueMap: {
            '5': 'Bank Deposit',
            'R': 'Bank Remittance',
            'B': 'Cash',
            'C': 'Cash on Delivery',
            '2': 'Check',
            'K': 'Credit Card',
            '4': 'Money Order',
            'P': 'On Credit',
            '3': 'Promissory Note',
            '1': 'Wire Transfer',
            'W': 'Withholding'
          }
        }, {
          name: 'paymentTerms',
          type: '_id_19',
          additional: false,
          required: true,
          title: 'paymentTerms',
          hidden: true
        }, {
          name: 'paymentTerms._identifier',
          type: 'text',
          hidden: true,
          title: 'paymentTerms'
        }, {
          name: 'invoiceTerms',
          type: '_id_150',
          additional: false,
          required: true,
          length: 60,
          title: 'invoiceTerms',
          valueMap: {
            'D': 'After Delivery',
            'O': 'After Order Delivered',
            'S': 'Customer Schedule After Delivery',
            'N': 'Do Not Invoice',
            'I': 'Immediate'
          }
        }, {
          name: 'deliveryTerms',
          type: '_id_151',
          additional: false,
          required: true,
          length: 60,
          title: 'deliveryTerms',
          valueMap: {
            'R': 'After Receipt',
            'A': 'Availability',
            'L': 'Complete Line',
            'O': 'Complete Order'
          }
        }, {
          name: 'freightCostRule',
          type: '_id_153',
          additional: false,
          required: true,
          length: 60,
          title: 'freightCostRule',
          valueMap: {
            'C': 'Calculated',
            'I': 'Freight included'
          }
        }, {
          name: 'freightAmount',
          type: '_id_12',
          additional: false,
          title: 'freightAmount'
        }, {
          name: 'deliveryMethod',
          type: '_id_152',
          additional: false,
          required: true,
          length: 60,
          title: 'deliveryMethod',
          valueMap: {
            'D': 'Delivery',
            'P': 'Pickup',
            'S': 'Shipper'
          }
        }, {
          name: 'shippingCompany',
          type: '_id_19',
          additional: false,
          title: 'shippingCompany',
          hidden: true
        }, {
          name: 'shippingCompany._identifier',
          type: 'text',
          hidden: true,
          title: 'shippingCompany'
        }, {
          name: 'charge',
          type: '_id_200',
          additional: false,
          title: 'charge',
          hidden: true
        }, {
          name: 'charge._identifier',
          type: 'text',
          hidden: true,
          title: 'charge'
        }, {
          name: 'chargeAmount',
          type: '_id_12',
          additional: false,
          title: 'chargeAmount'
        }, {
          name: 'priority',
          type: '_id_154',
          additional: false,
          required: true,
          length: 60,
          title: 'priority',
          valueMap: {
            '3': 'High',
            '7': 'Low',
            '5': 'Medium'
          }
        }, {
          name: 'summedLineAmount',
          type: '_id_12',
          additional: false,
          canSave: false,
          title: 'summedLineAmount'
        }, {
          name: 'grandTotalAmount',
          type: '_id_12',
          additional: false,
          canSave: false,
          title: 'grandTotalAmount'
        }, {
          name: 'warehouse',
          type: '_id_197',
          additional: false,
          required: true,
          title: 'warehouse',
          hidden: true
        }, {
          name: 'warehouse._identifier',
          type: 'text',
          hidden: true,
          title: 'warehouse'
        }, {
          name: 'priceList',
          type: '_id_19',
          additional: false,
          required: true,
          title: 'priceList',
          hidden: true
        }, {
          name: 'priceList._identifier',
          type: 'text',
          hidden: true,
          title: 'priceList'
        }, {
          name: 'priceIncludesTax',
          type: '_id_20',
          additional: false,
          title: 'priceIncludesTax'
        }, {
          name: 'salesCampaign',
          type: '_id_19',
          additional: false,
          title: 'salesCampaign',
          hidden: true
        }, {
          name: 'salesCampaign._identifier',
          type: 'text',
          hidden: true,
          title: 'salesCampaign'
        }, {
          name: 'project',
          type: '_id_800061',
          additional: false,
          title: 'project',
          hidden: true
        }, {
          name: 'project._identifier',
          type: 'text',
          hidden: true,
          title: 'project'
        }, {
          name: 'activity',
          type: '_id_19',
          additional: false,
          title: 'activity',
          hidden: true
        }, {
          name: 'activity._identifier',
          type: 'text',
          hidden: true,
          title: 'activity'
        }, {
          name: 'posted',
          type: '_id_234',
          additional: false,
          required: true,
          length: 60,
          title: 'posted',
          valueMap: {
            'N': 'Post',
            'd': 'Post: Disabled For Background',
            'D': 'Post: Document Disabled',
            'E': 'Post: Error',
            'C': 'Post: Error, No cost',
            'i': 'Post: Invalid Account',
            'b': 'Post: Not Balanced',
            'c': 'Post: Not Convertible (no rate)',
            'p': 'Post: Period Closed',
            'y': 'Post: Post Prepared',
            'Y': 'Unpost'
          }
        }, {
          name: 'userContact',
          type: '_id_19',
          additional: false,
          title: 'userContact',
          hidden: true
        }, {
          name: 'userContact._identifier',
          type: 'text',
          hidden: true,
          title: 'userContact'
        }, {
          name: 'copyFrom',
          type: '_id_28',
          additional: false,
          title: 'copyFrom'
        }, {
          name: 'dropShipPartner',
          type: '_id_173',
          additional: false,
          title: 'dropShipPartner',
          hidden: true
        }, {
          name: 'dropShipPartner._identifier',
          type: 'text',
          hidden: true,
          title: 'dropShipPartner'
        }, {
          name: 'dropShipLocation',
          type: '_id_159',
          additional: false,
          title: 'dropShipLocation',
          hidden: true
        }, {
          name: 'dropShipLocation._identifier',
          type: 'text',
          hidden: true,
          title: 'dropShipLocation'
        }, {
          name: 'dropShipContact',
          type: '_id_110',
          additional: false,
          title: 'dropShipContact',
          hidden: true
        }, {
          name: 'dropShipContact._identifier',
          type: 'text',
          hidden: true,
          title: 'dropShipContact'
        }, {
          name: 'selfService',
          type: '_id_20',
          additional: false,
          title: 'selfService'
        }, {
          name: 'trxOrganization',
          type: '_id_130',
          additional: false,
          title: 'trxOrganization',
          hidden: true
        }, {
          name: 'trxOrganization._identifier',
          type: 'text',
          hidden: true,
          title: 'trxOrganization'
        }, {
          name: 'stDimension',
          type: '_id_10',
          additional: false,
          length: 22,
          title: 'stDimension'
        }, {
          name: 'ndDimension',
          type: '_id_10',
          additional: false,
          length: 22,
          title: 'ndDimension'
        }, {
          name: 'deliveryNotes',
          type: '_id_14',
          additional: false,
          length: 2000,
          title: 'deliveryNotes'
        }, {
          name: 'incoterms',
          type: '_id_19',
          additional: false,
          title: 'incoterms',
          hidden: true
        }, {
          name: 'incoterms._identifier',
          type: 'text',
          hidden: true,
          title: 'incoterms'
        }, {
          name: 'iNCOTERMSDescription',
          type: '_id_14',
          additional: false,
          length: 255,
          title: 'iNCOTERMSDescription'
        }, {
          name: 'generateTemplate',
          type: '_id_28',
          additional: false,
          title: 'generateTemplate'
        }, {
          name: 'deliveryLocation',
          type: '_id_159',
          additional: false,
          title: 'deliveryLocation',
          hidden: true
        }, {
          name: 'deliveryLocation._identifier',
          type: 'text',
          hidden: true,
          title: 'deliveryLocation'
        }, {
          name: 'copyFromPO',
          type: '_id_28',
          additional: false,
          title: 'copyFromPO'
        }, {
          name: 'paymentMethod',
          type: '_id_19',
          additional: false,
          title: 'paymentMethod',
          hidden: true
        }, {
          name: 'paymentMethod._identifier',
          type: 'text',
          hidden: true,
          title: 'paymentMethod'
        }, {
          name: 'fINPaymentPriority',
          type: '_id_19',
          additional: false,
          title: 'fINPaymentPriority',
          hidden: true
        }, {
          name: 'fINPaymentPriority._identifier',
          type: 'text',
          hidden: true,
          title: 'fINPaymentPriority'
        }, {
          name: 'businessPartner.name',
          type: '_id_10',
          additional: true,
          required: true,
          length: 60,
          title: 'businessPartner.name'
        }]
      });
      this.viewForm = isc.OBViewForm.create({
        fields: [{
          name: 'organization',
          title: 'Organization',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'AD_Org_ID',
          inpColumnName: 'inpadOrgId',
          referencedKeyColumnName: 'AD_Org_ID',
          targetEntity: 'Organization',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'transactionDocument',
          title: 'Transaction Document',
          type: '_id_22F546D49D3A48E1B2B4F50446A8DE58',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_DocTypeTarget_ID',
          inpColumnName: 'inpcDoctypetargetId',
          referencedKeyColumnName: 'C_DocType_ID',
          targetEntity: 'DocumentType',
          required: true,
          firstFocusedField: true,
          width: '*',
          dummy: ''
        }, {
          name: 'documentNo',
          title: 'Document No.',
          type: '_id_10',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'DocumentNo',
          inpColumnName: 'inpdocumentno',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'orderDate',
          title: 'Order Date',
          type: '_id_15',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'DateOrdered',
          inpColumnName: 'inpdateordered',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          "width": "50%",
          dummy: ''
        }, {
          name: 'businessPartner',
          title: 'Business Partner',
          type: '_id_800057',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_BPartner_ID',
          inpColumnName: 'inpcBpartnerId',
          referencedKeyColumnName: 'C_BPartner_ID',
          targetEntity: 'BusinessPartner',
          required: true,
          firstFocusedField: true,
          selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
          popupTextMatchStyle: 'startsWith',
          textMatchStyle: 'startsWith',
          defaultPopupFilterField: 'name',
          displayField: 'name',
          valueField: 'bpid',
          pickListFields: [{
            title: ' ',
            name: 'name',
            disableFilter: true,
            canSort: false,
            type: 'text'
          }, {
            title: 'Location',
            name: 'locationname',
            disableFilter: true,
            canSort: false,
            type: '_id_10'
          }, {
            title: 'Contact',
            name: 'contactname',
            disableFilter: true,
            canSort: false,
            type: '_id_10'
          }],
          showSelectorGrid: true,
          selectorGridFields: [{
            title: 'Name',
            name: 'name',
            disableFilter: false,
            canSort: true,
            type: '_id_10',
            filterOnKeypress: true,
            canFilter: true,
            filterEditorType: 'OBTextFilterItem'
          }, {
            title: 'Value',
            name: 'value',
            disableFilter: false,
            canSort: true,
            type: '_id_10',
            filterOnKeypress: true,
            canFilter: true,
            filterEditorType: 'OBTextFilterItem'
          }, {
            title: 'Credit Line available',
            name: 'creditAvailable',
            disableFilter: false,
            canSort: true,
            type: '_id_12',
            canFilter: true,
            filterEditorType: 'OBNumberFilterItem'
          }, {
            title: 'Customer Balance',
            name: 'creditUsed',
            disableFilter: false,
            canSort: true,
            type: '_id_12',
            canFilter: true,
            filterEditorType: 'OBNumberFilterItem'
          }, {
            title: 'Location',
            name: 'locationname',
            disableFilter: false,
            canSort: true,
            type: '_id_10',
            filterOnKeypress: true,
            canFilter: true,
            filterEditorType: 'OBTextFilterItem'
          }, {
            title: 'Contact',
            name: 'contactname',
            disableFilter: false,
            canSort: true,
            type: '_id_10',
            filterOnKeypress: true,
            canFilter: true,
            filterEditorType: 'OBTextFilterItem'
          }, {
            title: 'Customer',
            name: 'customer',
            disableFilter: false,
            canSort: true,
            type: '_id_20',
            filterOnKeypress: true,
            canFilter: true,
            filterEditorType: 'OBYesNoItem'
          }, {
            title: 'Vendor',
            name: 'vendor',
            disableFilter: false,
            canSort: true,
            type: '_id_20',
            filterOnKeypress: true,
            canFilter: true,
            filterEditorType: 'OBYesNoItem'
          }, {
            title: 'Income',
            name: 'income',
            disableFilter: false,
            canSort: true,
            type: '_id_12',
            canFilter: true,
            filterEditorType: 'OBNumberFilterItem'
          }],
          outFields: {
            'id': {
              'fieldName': '',
              'suffix': ''
            },
            '_identifier': {
              'fieldName': '',
              'suffix': ''
            },
            'locationid': {
              'fieldName': 'locationid',
              'suffix': '_LOC'
            },
            'contactid': {
              'fieldName': 'contactid',
              'suffix': '_CON'
            }
          },
          extraSearchFields: ['value'],
          optionDataSource: OB.Datasource.create({
            createClassName: '',
            titleField: OB.Constants.IDENTIFIER,
            dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
            recordXPath: '/response/data',
            dataFormat: 'json',
            operationBindings: [{
              operationType: 'fetch',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'POST'
              }
            }, {
              operationType: 'add',
              dataProtocol: 'postMessage'
            }, {
              operationType: 'remove',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'DELETE'
              }
            }, {
              operationType: 'update',
              dataProtocol: 'postMessage',
              requestProperties: {
                httpMethod: 'PUT'
              }
            }],
            requestProperties: {
              params: {
                targetProperty: 'businessPartner',
                adTabId: '186',
                IsSelectorItem: 'true',
                columnName: 'C_BPartner_ID',
                _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
              }
            },
            fields: []
          }),
          whereClause: '',
          outHiddenInputPrefix: 'inpcBpartnerId',
          width: '*',
          dummy: ''
        }, {
          name: 'partnerAddress',
          title: 'Partner Address',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_BPartner_Location_ID',
          inpColumnName: 'inpcBpartnerLocationId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'priceList',
          title: 'Price List',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'M_PriceList_ID',
          inpColumnName: 'inpmPricelistId',
          referencedKeyColumnName: 'M_PriceList_ID',
          targetEntity: 'PricingPriceList',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'scheduledDeliveryDate',
          title: 'Scheduled Delivery Date',
          type: '_id_15',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'DatePromised',
          inpColumnName: 'inpdatepromised',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'OB' || context.ORDERTYPE === 'SO');
          },
          "width": "50%",
          dummy: ''
        }, {
          name: 'paymentMethod',
          title: 'Payment Method',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'FIN_Paymentmethod_ID',
          inpColumnName: 'inpfinPaymentmethodId',
          referencedKeyColumnName: 'Fin_Paymentmethod_ID',
          targetEntity: 'FIN_PaymentMethod',
          required: false,
          width: '*',
          dummy: ''
        }, {
          name: 'paymentTerms',
          title: 'Payment Terms',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_PaymentTerm_ID',
          inpColumnName: 'inpcPaymenttermId',
          referencedKeyColumnName: 'C_PaymentTerm_ID',
          targetEntity: 'FinancialMgmtPaymentTerm',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'warehouse',
          title: 'Warehouse',
          type: '_id_197',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'M_Warehouse_ID',
          inpColumnName: 'inpmWarehouseId',
          referencedKeyColumnName: 'M_Warehouse_ID',
          targetEntity: 'Warehouse',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'invoiceTerms',
          title: 'Invoice Terms',
          type: '_id_150',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'InvoiceRule',
          inpColumnName: 'inpinvoicerule',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP' || context.ORDERTYPE === 'PR' || context.ORDERTYPE === 'WR');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'userContact',
          title: 'User/Contact',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'AD_User_ID',
          inpColumnName: 'inpadUserId',
          referencedKeyColumnName: 'AD_User_ID',
          targetEntity: 'ADUser',
          required: false,
          width: '*',
          dummy: ''
        }, {
          name: 'project',
          title: 'Project',
          type: '_id_800061',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_Project_ID',
          inpColumnName: 'inpcProjectId',
          referencedKeyColumnName: 'C_Project_ID',
          targetEntity: 'Project',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.$Element_PJ === 'Y');
          },
          displayField: 'project._identifier',
          valueField: 'project',
          showPickerIcon: true,
          "width": "*",
          "searchUrl": "/info/Project.html",
          "inFields": [{
            "columnName": "inpcBpartnerId",
            "parameterName": "inpc_bpartner_id"
          }, {
            "columnName": "inpadOrgId",
            "parameterName": "inpAD_Org_ID"
          }],
          "outFields": [],
          dummy: ''
        }, {
          name: '402880E72F1C15A5012F1C7AA98B00E8',
          title: 'More Information',
          type: 'OBSectionItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: true,
          hasDefaultValue: false,
          sectionExpanded: false,
          defaultValue: 'More Information',
          itemIds: ['orderReference', 'salesRepresentative', 'description', 'invoiceAddress', 'deliveryLocation', 'deliveryMethod', 'shippingCompany', 'deliveryTerms', 'freightCostRule', 'freightAmount', 'printDiscount', 'priority', 'salesCampaign', 'activity', 'trxOrganization', 'ndDimension', 'stDimension'],
          dummy: ''
        }, {
          name: 'orderReference',
          title: 'Order Reference',
          type: '_id_10',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'POReference',
          inpColumnName: 'inpporeference',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          width: '*',
          dummy: ''
        }, {
          name: 'salesRepresentative',
          title: 'Sales Representative',
          type: '_id_190',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'SalesRep_ID',
          inpColumnName: 'inpsalesrepId',
          referencedKeyColumnName: 'AD_User_ID',
          targetEntity: 'ADUser',
          required: false,
          width: '*',
          dummy: ''
        }, {
          name: 'description',
          title: 'Description',
          type: '_id_14',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 2,
          rowSpan: 2,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'Description',
          inpColumnName: 'inpdescription',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          width: '*',
          dummy: ''
        }, {
          name: 'invoiceAddress',
          title: 'Invoice Address',
          type: '_id_159',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'BillTo_ID',
          inpColumnName: 'inpbilltoId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation',
          required: true,
          width: '*',
          dummy: ''
        }, {
          name: 'deliveryLocation',
          title: 'Delivery Location',
          type: '_id_159',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'Delivery_Location_ID',
          inpColumnName: 'inpdeliveryLocationId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation',
          required: false,
          width: '*',
          dummy: ''
        }, {
          name: 'deliveryMethod',
          title: 'Delivery Method',
          type: '_id_152',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'DeliveryViaRule',
          inpColumnName: 'inpdeliveryviarule',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          redrawOnChange: true,
          changed: function (form, item, value) {
            if (this.pickValue && !this._pickedValue) {
              return;
            }
            this.Super('changed', arguments);
            form.onFieldChanged(form, item, value);
            form.view.toolBar.refreshCustomButtonsView(form.view);
          },
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP' || context.ORDERTYPE === 'PR' || context.ORDERTYPE === 'WR' || context.ORDERTYPE === 'WI');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'shippingCompany',
          title: 'Shipping Company',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'M_Shipper_ID',
          inpColumnName: 'inpmShipperId',
          referencedKeyColumnName: 'M_Shipper_ID',
          targetEntity: 'ShippingShippingCompany',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (currentValues.deliveryMethod === 'S' && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP' || context.ORDERTYPE === 'PR' || context.ORDERTYPE === 'WR' || context.ORDERTYPE === 'WI'));
          },
          width: '*',
          dummy: ''
        }, {
          name: 'deliveryTerms',
          title: 'Delivery Terms',
          type: '_id_151',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'DeliveryRule',
          inpColumnName: 'inpdeliveryrule',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'freightCostRule',
          title: 'Freight Cost Rule',
          type: '_id_153',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'FreightCostRule',
          inpColumnName: 'inpfreightcostrule',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          redrawOnChange: true,
          changed: function (form, item, value) {
            if (this.pickValue && !this._pickedValue) {
              return;
            }
            this.Super('changed', arguments);
            form.onFieldChanged(form, item, value);
            form.view.toolBar.refreshCustomButtonsView(form.view);
          },
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'SO');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'freightAmount',
          title: 'Freight Amount',
          type: '_id_12',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'FreightAmt',
          inpColumnName: 'inpfreightamt',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'SO' && currentValues.freightCostRule === 'F');
          },
          "width": "50%",
          dummy: ''
        }, {
          name: 'printDiscount',
          title: 'Print Discount',
          type: '_id_20',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'IsDiscountPrinted',
          inpColumnName: 'inpisdiscountprinted',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          "width": 1,
          "overflow": "visible",
          dummy: ''
        }, {
          name: 'priority',
          title: 'Priority',
          type: '_id_154',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'PriorityRule',
          inpColumnName: 'inppriorityrule',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: true,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'salesCampaign',
          title: 'Sales Campaign',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_Campaign_ID',
          inpColumnName: 'inpcCampaignId',
          referencedKeyColumnName: 'C_Campaign_ID',
          targetEntity: 'MarketingCampaign',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.$Element_MC === 'Y');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'activity',
          title: 'Activity',
          type: '_id_19',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'C_Activity_ID',
          inpColumnName: 'inpcActivityId',
          referencedKeyColumnName: 'C_Activity_ID',
          targetEntity: 'MaterialMgmtABCActivity',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.$Element_AY === 'Y');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'trxOrganization',
          title: 'Trx Organization',
          type: '_id_130',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'AD_OrgTrx_ID',
          inpColumnName: 'inpadOrgtrxId',
          referencedKeyColumnName: 'AD_Org_ID',
          targetEntity: 'Organization',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.$Element_OT === 'Y');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'ndDimension',
          title: '2nd Dimension',
          type: '_id_10',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'User2_ID',
          inpColumnName: 'inpuser2Id',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.$Element_U2 === 'Y');
          },
          width: '*',
          dummy: ''
        }, {
          name: 'stDimension',
          title: '1st Dimension',
          type: '_id_10',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'User1_ID',
          inpColumnName: 'inpuser1Id',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          showIf: function (item, value, form, values) {
            var context = form.view.getContextInfo(false, true, true),
                currentValues = values || form.view.getCurrentValues();
            OB.Utilities.fixNull250(currentValues);
            return !this.hiddenInForm && context && (context.$Element_U1 === 'Y');
          },
          width: '*',
          dummy: ''
        }, {
          name: '1000100001',
          title: 'Audit',
          type: 'OBAuditSectionItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          sectionExpanded: false,
          defaultValue: 'Audit',
          itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
          dummy: ''
        }, {
          name: 'creationDate',
          title: 'Creation Date',
          type: '_id_16',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: false,
          hasDefaultValue: false,
          columnName: '',
          inpColumnName: '',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          'width': '*',
          dummy: ''
        }, {
          name: 'createdBy',
          title: 'Created By',
          type: '_id_30',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: false,
          hasDefaultValue: false,
          columnName: '',
          inpColumnName: '',
          referencedKeyColumnName: '',
          targetEntity: 'User',
          required: false,
          displayField: 'createdBy._identifier',
          valueField: 'createdBy',
          showPickerIcon: true,
          'width': '*',
          dummy: ''
        }, {
          name: 'updated',
          title: 'Updated',
          type: '_id_16',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: false,
          hasDefaultValue: false,
          columnName: '',
          inpColumnName: '',
          referencedKeyColumnName: '',
          targetEntity: '',
          required: false,
          'width': '*',
          dummy: ''
        }, {
          name: 'updatedBy',
          title: 'Updated By',
          type: '_id_30',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: false,
          hasDefaultValue: false,
          columnName: '',
          inpColumnName: '',
          referencedKeyColumnName: '',
          targetEntity: 'User',
          required: false,
          displayField: 'updatedBy._identifier',
          valueField: 'updatedBy',
          showPickerIcon: true,
          'width': '*',
          dummy: ''
        }, {
          name: '_notes_',
          title: 'dummy',
          type: 'OBNoteSectionItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          sectionExpanded: false,
          defaultValue: 'dummy',
          itemIds: ['_notes_Canvas'],
          dummy: ''
        }, {
          name: '_notes_Canvas',
          title: 'dummy',
          type: 'OBNoteCanvasItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          dummy: ''
        }, {
          name: '_linkedItems_',
          title: 'dummy',
          type: 'OBLinkedItemSectionItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          sectionExpanded: false,
          defaultValue: 'dummy',
          itemIds: ['_linkedItems_Canvas'],
          dummy: ''
        }, {
          name: '_linkedItems_Canvas',
          title: 'dummy',
          type: 'OBLinkedItemCanvasItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          dummy: ''
        }, {
          name: '_attachments_',
          title: 'dummy',
          type: 'OBAttachmentsSectionItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          sectionExpanded: false,
          defaultValue: 'dummy',
          itemIds: ['_attachments_Canvas'],
          dummy: ''
        }, {
          name: '_attachments_Canvas',
          title: '',
          type: 'OBAttachmentCanvasItem',
          disabled: false,
          readonly: false,
          updatable: true,
          parentProperty: false,
          colSpan: 4,
          rowSpan: 1,
          startRow: true,
          endRow: true,
          personalizable: false,
          hasDefaultValue: false,
          dummy: ''
        }, {
          name: 'documentStatus',
          title: 'Document Status',
          type: '_id_FF80818130217A350130218D802B0011',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'DocStatus',
          inpColumnName: 'inpdocstatus',
          referencedKeyColumnName: '',
          targetEntity: '',
          visible: false,
          displayed: false,
          alwaysTakeSpace: false,
          required: true,
          width: '',
          dummy: ''
        }, {
          name: 'grandTotalAmount',
          title: 'Total Gross Amount',
          type: '_id_12',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'GrandTotal',
          inpColumnName: 'inpgrandtotal',
          referencedKeyColumnName: '',
          targetEntity: '',
          visible: false,
          displayed: false,
          alwaysTakeSpace: false,
          required: true,
          "width": "",
          dummy: ''
        }, {
          name: 'summedLineAmount',
          title: 'Total Net Amount',
          type: '_id_12',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: false,
          columnName: 'TotalLines',
          inpColumnName: 'inptotallines',
          referencedKeyColumnName: '',
          targetEntity: '',
          visible: false,
          displayed: false,
          alwaysTakeSpace: false,
          required: true,
          "width": "",
          dummy: ''
        }, {
          name: 'currency',
          title: 'Currency',
          type: '_id_19',
          disabled: true,
          readonly: true,
          updatable: false,
          parentProperty: false,
          colSpan: 1,
          rowSpan: 1,
          startRow: false,
          endRow: false,
          personalizable: true,
          hasDefaultValue: true,
          columnName: 'C_Currency_ID',
          inpColumnName: 'inpcCurrencyId',
          referencedKeyColumnName: 'C_Currency_ID',
          targetEntity: 'Currency',
          visible: false,
          displayed: false,
          alwaysTakeSpace: false,
          required: true,
          width: '',
          dummy: ''
        }],
        statusBarFields: ['documentStatus', 'grandTotalAmount', 'summedLineAmount', 'currency'],
        obFormProperties: {
          onFieldChanged: function (form, item, value) {
            var f = form || this,
                context = this.view.getContextInfo(false, true),
                currentValues = f.view.getCurrentValues(),
                otherItem;
            otherItem = f.getItem('organization');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('transactionDocument');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('documentNo');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('orderDate');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('businessPartner');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('partnerAddress');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('priceList');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('scheduledDeliveryDate');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('paymentMethod');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('paymentTerms');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('warehouse');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('userContact');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('project');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.posted === 'Y') {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('invoiceAddress');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('deliveryMethod');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('deliveryTerms');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('priority');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('salesCampaign');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.posted === 'Y') {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('activity');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.posted === 'Y') {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('trxOrganization');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('ndDimension');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
            otherItem = f.getItem('stDimension');
            if (otherItem && otherItem.disable && otherItem.enable) {
              if (f.readOnly) {
                otherItem.disable();
              } else if (currentValues.processed === true) {
                otherItem.disable();
              } else {
                otherItem.enable();
              }
            }
          }
        }
      });
      this.viewGrid = isc.OBViewGrid.create({
        uiPattern: 'STD',
        fields: [{
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'AD_Org_ID',
            inpColumnName: 'inpadOrgId',
            referencedKeyColumnName: 'AD_Org_ID',
            targetEntity: 'Organization',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'organization._identifier',
          valueField: 'organization',
          foreignKeyField: true,
          name: 'organization',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Organization',
          prompt: 'Organization',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'AD_Org_ID',
          inpColumnName: 'inpadOrgId',
          referencedKeyColumnName: 'AD_Org_ID',
          targetEntity: 'Organization'
        }, {
          autoExpand: false,
          type: '_id_10',
          editorProperties: {
            width: '*',
            columnName: 'DocumentNo',
            inpColumnName: 'inpdocumentno',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'documentNo')])",
          width: isc.OBGrid.getDefaultColumnWidth(20),
          name: 'documentNo',
          canExport: true,
          canHide: true,
          editorType: 'OBTextItem',
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextFilterItem',
          title: 'Document No.',
          prompt: 'Document No.',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'DocumentNo',
          inpColumnName: 'inpdocumentno',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_22F546D49D3A48E1B2B4F50446A8DE58',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_DocTypeTarget_ID',
            inpColumnName: 'inpcDoctypetargetId',
            referencedKeyColumnName: 'C_DocType_ID',
            targetEntity: 'DocumentType',
            disabled: false,
            readonly: false,
            updatable: true,
            firstFocusedField: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'transactionDocument._identifier',
          valueField: 'transactionDocument',
          foreignKeyField: true,
          name: 'transactionDocument',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'transactionDocument')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Transaction Document',
          prompt: 'Transaction Document',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'C_DocTypeTarget_ID',
          inpColumnName: 'inpcDoctypetargetId',
          referencedKeyColumnName: 'C_DocType_ID',
          targetEntity: 'DocumentType'
        }, {
          autoExpand: false,
          type: '_id_15',
          cellAlign: 'left',
          editorProperties: {
            "width": "50%",
            columnName: 'DateOrdered',
            inpColumnName: 'inpdateordered',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'orderDate',
          canExport: true,
          canHide: true,
          editorType: 'OBDateItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBMiniDateRangeItem',
          title: 'Order Date',
          prompt: 'Order Date',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'DateOrdered',
          inpColumnName: 'inpdateordered',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_800057',
          editorProperties: {
            selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
            popupTextMatchStyle: 'startsWith',
            textMatchStyle: 'startsWith',
            defaultPopupFilterField: 'name',
            displayField: 'name',
            valueField: 'bpid',
            pickListFields: [{
              title: ' ',
              name: 'name',
              disableFilter: true,
              canSort: false,
              type: 'text'
            }, {
              title: 'Location',
              name: 'locationname',
              disableFilter: true,
              canSort: false,
              type: '_id_10'
            }, {
              title: 'Contact',
              name: 'contactname',
              disableFilter: true,
              canSort: false,
              type: '_id_10'
            }],
            showSelectorGrid: true,
            selectorGridFields: [{
              title: 'Name',
              name: 'name',
              disableFilter: false,
              canSort: true,
              type: '_id_10',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextFilterItem'
            }, {
              title: 'Value',
              name: 'value',
              disableFilter: false,
              canSort: true,
              type: '_id_10',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextFilterItem'
            }, {
              title: 'Credit Line available',
              name: 'creditAvailable',
              disableFilter: false,
              canSort: true,
              type: '_id_12',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem'
            }, {
              title: 'Customer Balance',
              name: 'creditUsed',
              disableFilter: false,
              canSort: true,
              type: '_id_12',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem'
            }, {
              title: 'Location',
              name: 'locationname',
              disableFilter: false,
              canSort: true,
              type: '_id_10',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextFilterItem'
            }, {
              title: 'Contact',
              name: 'contactname',
              disableFilter: false,
              canSort: true,
              type: '_id_10',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextFilterItem'
            }, {
              title: 'Customer',
              name: 'customer',
              disableFilter: false,
              canSort: true,
              type: '_id_20',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem'
            }, {
              title: 'Vendor',
              name: 'vendor',
              disableFilter: false,
              canSort: true,
              type: '_id_20',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem'
            }, {
              title: 'Income',
              name: 'income',
              disableFilter: false,
              canSort: true,
              type: '_id_12',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem'
            }],
            outFields: {
              'id': {
                'fieldName': '',
                'suffix': ''
              },
              '_identifier': {
                'fieldName': '',
                'suffix': ''
              },
              'locationid': {
                'fieldName': 'locationid',
                'suffix': '_LOC'
              },
              'contactid': {
                'fieldName': 'contactid',
                'suffix': '_CON'
              }
            },
            extraSearchFields: ['value'],
            optionDataSource: OB.Datasource.create({
              createClassName: '',
              titleField: OB.Constants.IDENTIFIER,
              dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
              recordXPath: '/response/data',
              dataFormat: 'json',
              operationBindings: [{
                operationType: 'fetch',
                dataProtocol: 'postParams',
                requestProperties: {
                  httpMethod: 'POST'
                }
              }, {
                operationType: 'add',
                dataProtocol: 'postMessage'
              }, {
                operationType: 'remove',
                dataProtocol: 'postParams',
                requestProperties: {
                  httpMethod: 'DELETE'
                }
              }, {
                operationType: 'update',
                dataProtocol: 'postMessage',
                requestProperties: {
                  httpMethod: 'PUT'
                }
              }],
              requestProperties: {
                params: {
                  targetProperty: 'businessPartner',
                  adTabId: '186',
                  IsSelectorItem: 'true',
                  columnName: 'C_BPartner_ID',
                  _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
                }
              },
              fields: []
            }),
            whereClause: '',
            outHiddenInputPrefix: 'inpcBpartnerId',
            width: '*',
            columnName: 'C_BPartner_ID',
            inpColumnName: 'inpcBpartnerId',
            referencedKeyColumnName: 'C_BPartner_ID',
            targetEntity: 'BusinessPartner',
            disabled: false,
            readonly: false,
            updatable: true,
            firstFocusedField: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'businessPartner._identifier',
          valueField: 'businessPartner',
          foreignKeyField: true,
          name: 'businessPartner',
          canExport: true,
          canHide: true,
          editorType: 'OBSelectorItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'businessPartner')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Business Partner',
          prompt: 'Business Partner',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'C_BPartner_ID',
          inpColumnName: 'inpcBpartnerId',
          referencedKeyColumnName: 'C_BPartner_ID',
          targetEntity: 'BusinessPartner'
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_BPartner_Location_ID',
            inpColumnName: 'inpcBpartnerLocationId',
            referencedKeyColumnName: 'C_BPartner_Location_ID',
            targetEntity: 'BusinessPartnerLocation',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'partnerAddress._identifier',
          valueField: 'partnerAddress',
          foreignKeyField: true,
          name: 'partnerAddress',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'partnerAddress')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Partner Address',
          prompt: 'Partner Address',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'C_BPartner_Location_ID',
          inpColumnName: 'inpcBpartnerLocationId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation'
        }, {
          autoExpand: false,
          type: '_id_12',
          editorProperties: {
            "width": "",
            columnName: 'GrandTotal',
            inpColumnName: 'inpgrandtotal',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: false
          },
          name: 'grandTotalAmount',
          canExport: true,
          canHide: true,
          editorType: 'OBNumberItem',
          canFilter: true,
          filterEditorType: 'OBNumberFilterItem',
          title: 'Total Gross Amount',
          prompt: 'Total Gross Amount',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'GrandTotal',
          inpColumnName: 'inpgrandtotal',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_Currency_ID',
            inpColumnName: 'inpcCurrencyId',
            referencedKeyColumnName: 'C_Currency_ID',
            targetEntity: 'Currency',
            disabled: true,
            readonly: true,
            updatable: false
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'currency._identifier',
          valueField: 'currency',
          foreignKeyField: true,
          name: 'currency',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'currency')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Currency',
          prompt: 'Currency',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'C_Currency_ID',
          inpColumnName: 'inpcCurrencyId',
          referencedKeyColumnName: 'C_Currency_ID',
          targetEntity: 'Currency'
        }, {
          autoExpand: false,
          type: '_id_FF80818130217A350130218D802B0011',
          editorProperties: {
            width: '',
            columnName: 'DocStatus',
            inpColumnName: 'inpdocstatus',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: false,
            redrawOnChange: true,
            changed: function (form, item, value) {
              if (this.pickValue && !this._pickedValue) {
                return;
              }
              this.Super('changed', arguments);
              form.onFieldChanged(form, item, value);
              form.view.toolBar.refreshCustomButtonsView(form.view);
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'documentStatus')])",
          width: isc.OBGrid.getDefaultColumnWidth(21),
          name: 'documentStatus',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Document Status',
          prompt: 'Document Status',
          required: true,
          escapeHTML: true,
          showIf: 'true',
          columnName: 'DocStatus',
          inpColumnName: 'inpdocstatus',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'M_PriceList_ID',
            inpColumnName: 'inpmPricelistId',
            referencedKeyColumnName: 'M_PriceList_ID',
            targetEntity: 'PricingPriceList',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'priceList._identifier',
          valueField: 'priceList',
          foreignKeyField: true,
          name: 'priceList',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'priceList')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Price List',
          prompt: 'Price List',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'M_PriceList_ID',
          inpColumnName: 'inpmPricelistId',
          referencedKeyColumnName: 'M_PriceList_ID',
          targetEntity: 'PricingPriceList'
        }, {
          autoExpand: false,
          type: '_id_15',
          cellAlign: 'left',
          editorProperties: {
            "width": "50%",
            columnName: 'DatePromised',
            inpColumnName: 'inpdatepromised',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'OB' || context.ORDERTYPE === 'SO');
            }
          },
          name: 'scheduledDeliveryDate',
          canExport: true,
          canHide: true,
          editorType: 'OBDateItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBMiniDateRangeItem',
          title: 'Scheduled Delivery Date',
          prompt: 'Scheduled Delivery Date',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DatePromised',
          inpColumnName: 'inpdatepromised',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'FIN_Paymentmethod_ID',
            inpColumnName: 'inpfinPaymentmethodId',
            referencedKeyColumnName: 'Fin_Paymentmethod_ID',
            targetEntity: 'FIN_PaymentMethod',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(32),
          displayField: 'paymentMethod._identifier',
          valueField: 'paymentMethod',
          foreignKeyField: true,
          name: 'paymentMethod',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'paymentMethod')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Payment Method',
          prompt: 'Payment Method',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'FIN_Paymentmethod_ID',
          inpColumnName: 'inpfinPaymentmethodId',
          referencedKeyColumnName: 'Fin_Paymentmethod_ID',
          targetEntity: 'FIN_PaymentMethod'
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_PaymentTerm_ID',
            inpColumnName: 'inpcPaymenttermId',
            referencedKeyColumnName: 'C_PaymentTerm_ID',
            targetEntity: 'FinancialMgmtPaymentTerm',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'paymentTerms._identifier',
          valueField: 'paymentTerms',
          foreignKeyField: true,
          name: 'paymentTerms',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'paymentTerms')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Payment Terms',
          prompt: 'Payment Terms',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_PaymentTerm_ID',
          inpColumnName: 'inpcPaymenttermId',
          referencedKeyColumnName: 'C_PaymentTerm_ID',
          targetEntity: 'FinancialMgmtPaymentTerm'
        }, {
          autoExpand: true,
          type: '_id_197',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'M_Warehouse_ID',
            inpColumnName: 'inpmWarehouseId',
            referencedKeyColumnName: 'M_Warehouse_ID',
            targetEntity: 'Warehouse',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'warehouse._identifier',
          valueField: 'warehouse',
          foreignKeyField: true,
          name: 'warehouse',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'warehouse')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Warehouse',
          prompt: 'Warehouse',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'M_Warehouse_ID',
          inpColumnName: 'inpmWarehouseId',
          referencedKeyColumnName: 'M_Warehouse_ID',
          targetEntity: 'Warehouse'
        }, {
          autoExpand: false,
          type: '_id_150',
          editorProperties: {
            width: '*',
            columnName: 'InvoiceRule',
            inpColumnName: 'inpinvoicerule',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP' || context.ORDERTYPE === 'PR' || context.ORDERTYPE === 'WR');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'invoiceTerms')])",
          width: isc.OBGrid.getDefaultColumnWidth(44),
          name: 'invoiceTerms',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Invoice Terms',
          prompt: 'Invoice Terms',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'InvoiceRule',
          inpColumnName: 'inpinvoicerule',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_12',
          editorProperties: {
            "width": "",
            columnName: 'TotalLines',
            inpColumnName: 'inptotallines',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: false
          },
          name: 'summedLineAmount',
          canExport: true,
          canHide: true,
          editorType: 'OBNumberItem',
          canFilter: true,
          filterEditorType: 'OBNumberFilterItem',
          title: 'Total Net Amount',
          prompt: 'Total Net Amount',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'TotalLines',
          inpColumnName: 'inptotallines',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'AD_User_ID',
            inpColumnName: 'inpadUserId',
            referencedKeyColumnName: 'AD_User_ID',
            targetEntity: 'ADUser',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'userContact._identifier',
          valueField: 'userContact',
          foreignKeyField: true,
          name: 'userContact',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'userContact')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'User/Contact',
          prompt: 'User/Contact',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'AD_User_ID',
          inpColumnName: 'inpadUserId',
          referencedKeyColumnName: 'AD_User_ID',
          targetEntity: 'ADUser'
        }, {
          autoExpand: true,
          type: '_id_800061',
          editorProperties: {
            "width": "*",
            "searchUrl": "/info/Project.html",
            "inFields": [{
              "columnName": "inpcBpartnerId",
              "parameterName": "inpc_bpartner_id"
            }, {
              "columnName": "inpadOrgId",
              "parameterName": "inpAD_Org_ID"
            }],
            "outFields": [],
            columnName: 'C_Project_ID',
            inpColumnName: 'inpcProjectId',
            referencedKeyColumnName: 'C_Project_ID',
            targetEntity: 'Project',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.$Element_PJ === 'Y');
            }
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'project._identifier',
          valueField: 'project',
          foreignKeyField: true,
          name: 'project',
          canExport: true,
          canHide: true,
          editorType: 'OBSearchItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'project')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Project',
          prompt: 'Project',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_Project_ID',
          inpColumnName: 'inpcProjectId',
          referencedKeyColumnName: 'C_Project_ID',
          targetEntity: 'Project'
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_DocType_ID',
            inpColumnName: 'inpcDoctypeId',
            referencedKeyColumnName: 'C_DocType_ID',
            targetEntity: 'DocumentType',
            disabled: true,
            readonly: true,
            updatable: false
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'documentType._identifier',
          valueField: 'documentType',
          foreignKeyField: true,
          name: 'documentType',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'documentType')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Document Type',
          prompt: 'Document Type',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_DocType_ID',
          inpColumnName: 'inpcDoctypeId',
          referencedKeyColumnName: 'C_DocType_ID',
          targetEntity: 'DocumentType'
        }, {
          autoExpand: true,
          type: '_id_10',
          editorProperties: {
            width: '*',
            columnName: 'POReference',
            inpColumnName: 'inpporeference',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'orderReference')])",
          width: isc.OBGrid.getDefaultColumnWidth(11),
          name: 'orderReference',
          canExport: true,
          canHide: true,
          editorType: 'OBTextItem',
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextFilterItem',
          title: 'Order Reference',
          prompt: 'Order Reference',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'POReference',
          inpColumnName: 'inpporeference',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_190',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'SalesRep_ID',
            inpColumnName: 'inpsalesrepId',
            referencedKeyColumnName: 'AD_User_ID',
            targetEntity: 'ADUser',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'salesRepresentative._identifier',
          valueField: 'salesRepresentative',
          foreignKeyField: true,
          name: 'salesRepresentative',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'salesRepresentative')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Sales Representative',
          prompt: 'Sales Representative',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'SalesRep_ID',
          inpColumnName: 'inpsalesrepId',
          referencedKeyColumnName: 'AD_User_ID',
          targetEntity: 'ADUser'
        }, {
          autoExpand: true,
          type: '_id_14',
          editorProperties: {
            width: '*',
            columnName: 'Description',
            inpColumnName: 'inpdescription',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'description')])",
          width: isc.OBGrid.getDefaultColumnWidth(60),
          name: 'description',
          canExport: true,
          canHide: true,
          editorType: 'OBPopUpTextAreaItem',
          canSort: false,
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextItem',
          title: 'Description',
          prompt: 'Description',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'Description',
          inpColumnName: 'inpdescription',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_159',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'BillTo_ID',
            inpColumnName: 'inpbilltoId',
            referencedKeyColumnName: 'C_BPartner_Location_ID',
            targetEntity: 'BusinessPartnerLocation',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'invoiceAddress._identifier',
          valueField: 'invoiceAddress',
          foreignKeyField: true,
          name: 'invoiceAddress',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'invoiceAddress')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Invoice Address',
          prompt: 'Invoice Address',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'BillTo_ID',
          inpColumnName: 'inpbilltoId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation'
        }, {
          autoExpand: true,
          type: '_id_159',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'Delivery_Location_ID',
            inpColumnName: 'inpdeliveryLocationId',
            referencedKeyColumnName: 'C_BPartner_Location_ID',
            targetEntity: 'BusinessPartnerLocation',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'deliveryLocation._identifier',
          valueField: 'deliveryLocation',
          foreignKeyField: true,
          name: 'deliveryLocation',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'deliveryLocation')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Delivery Location',
          prompt: 'Delivery Location',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'Delivery_Location_ID',
          inpColumnName: 'inpdeliveryLocationId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation'
        }, {
          autoExpand: false,
          type: '_id_152',
          editorProperties: {
            width: '*',
            columnName: 'DeliveryViaRule',
            inpColumnName: 'inpdeliveryviarule',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            redrawOnChange: true,
            changed: function (form, item, value) {
              if (this.pickValue && !this._pickedValue) {
                return;
              }
              this.Super('changed', arguments);
              form.onFieldChanged(form, item, value);
              form.view.toolBar.refreshCustomButtonsView(form.view);
            },
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP' || context.ORDERTYPE === 'PR' || context.ORDERTYPE === 'WR' || context.ORDERTYPE === 'WI');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'deliveryMethod')])",
          width: isc.OBGrid.getDefaultColumnWidth(21),
          name: 'deliveryMethod',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Delivery Method',
          prompt: 'Delivery Method',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DeliveryViaRule',
          inpColumnName: 'inpdeliveryviarule',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'M_Shipper_ID',
            inpColumnName: 'inpmShipperId',
            referencedKeyColumnName: 'M_Shipper_ID',
            targetEntity: 'ShippingShippingCompany',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (currentValues.deliveryMethod === 'S' && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP' || context.ORDERTYPE === 'PR' || context.ORDERTYPE === 'WR' || context.ORDERTYPE === 'WI'));
            }
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'shippingCompany._identifier',
          valueField: 'shippingCompany',
          foreignKeyField: true,
          name: 'shippingCompany',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'shippingCompany')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Shipping Company',
          prompt: 'Shipping Company',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'M_Shipper_ID',
          inpColumnName: 'inpmShipperId',
          referencedKeyColumnName: 'M_Shipper_ID',
          targetEntity: 'ShippingShippingCompany'
        }, {
          autoExpand: false,
          type: '_id_151',
          editorProperties: {
            width: '*',
            columnName: 'DeliveryRule',
            inpColumnName: 'inpdeliveryrule',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'deliveryTerms')])",
          width: isc.OBGrid.getDefaultColumnWidth(21),
          name: 'deliveryTerms',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Delivery Terms',
          prompt: 'Delivery Terms',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DeliveryRule',
          inpColumnName: 'inpdeliveryrule',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_153',
          editorProperties: {
            width: '*',
            columnName: 'FreightCostRule',
            inpColumnName: 'inpfreightcostrule',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            redrawOnChange: true,
            changed: function (form, item, value) {
              if (this.pickValue && !this._pickedValue) {
                return;
              }
              this.Super('changed', arguments);
              form.onFieldChanged(form, item, value);
              form.view.toolBar.refreshCustomButtonsView(form.view);
            },
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'SO');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'freightCostRule')])",
          width: isc.OBGrid.getDefaultColumnWidth(21),
          name: 'freightCostRule',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Freight Cost Rule',
          prompt: 'Freight Cost Rule',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'FreightCostRule',
          inpColumnName: 'inpfreightcostrule',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_12',
          editorProperties: {
            "width": "50%",
            columnName: 'FreightAmt',
            inpColumnName: 'inpfreightamt',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'SO' && currentValues.freightCostRule === 'F');
            }
          },
          name: 'freightAmount',
          canExport: true,
          canHide: true,
          editorType: 'OBNumberItem',
          canFilter: true,
          filterEditorType: 'OBNumberFilterItem',
          title: 'Freight Amount',
          prompt: 'Freight Amount',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'FreightAmt',
          inpColumnName: 'inpfreightamt',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsDiscountPrinted',
            inpColumnName: 'inpisdiscountprinted',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'printDiscount',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Print Discount',
          prompt: 'Print Discount',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsDiscountPrinted',
          inpColumnName: 'inpisdiscountprinted',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_154',
          editorProperties: {
            width: '*',
            columnName: 'PriorityRule',
            inpColumnName: 'inppriorityrule',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.ORDERTYPE === 'SO' || context.ORDERTYPE === 'WP');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'priority')])",
          width: isc.OBGrid.getDefaultColumnWidth(21),
          name: 'priority',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Priority',
          prompt: 'Priority',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'PriorityRule',
          inpColumnName: 'inppriorityrule',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_Campaign_ID',
            inpColumnName: 'inpcCampaignId',
            referencedKeyColumnName: 'C_Campaign_ID',
            targetEntity: 'MarketingCampaign',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.$Element_MC === 'Y');
            }
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'salesCampaign._identifier',
          valueField: 'salesCampaign',
          foreignKeyField: true,
          name: 'salesCampaign',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'salesCampaign')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Sales Campaign',
          prompt: 'Sales Campaign',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_Campaign_ID',
          inpColumnName: 'inpcCampaignId',
          referencedKeyColumnName: 'C_Campaign_ID',
          targetEntity: 'MarketingCampaign'
        }, {
          autoExpand: false,
          type: '_id_12',
          editorProperties: {
            "width": "",
            columnName: 'ChargeAmt',
            inpColumnName: 'inpchargeamt',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'chargeAmount',
          canExport: true,
          canHide: true,
          editorType: 'OBNumberItem',
          canFilter: true,
          filterEditorType: 'OBNumberFilterItem',
          title: 'Charge Amount',
          prompt: 'Charge Amount',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'ChargeAmt',
          inpColumnName: 'inpchargeamt',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_200',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_Charge_ID',
            inpColumnName: 'inpcChargeId',
            referencedKeyColumnName: 'C_Charge_ID',
            targetEntity: 'FinancialMgmtGLCharge',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'charge._identifier',
          valueField: 'charge',
          foreignKeyField: true,
          name: 'charge',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'charge')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Charge',
          prompt: 'Charge',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_Charge_ID',
          inpColumnName: 'inpcChargeId',
          referencedKeyColumnName: 'C_Charge_ID',
          targetEntity: 'FinancialMgmtGLCharge'
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_Activity_ID',
            inpColumnName: 'inpcActivityId',
            referencedKeyColumnName: 'C_Activity_ID',
            targetEntity: 'MaterialMgmtABCActivity',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.$Element_AY === 'Y');
            }
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'activity._identifier',
          valueField: 'activity',
          foreignKeyField: true,
          name: 'activity',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'activity')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Activity',
          prompt: 'Activity',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_Activity_ID',
          inpColumnName: 'inpcActivityId',
          referencedKeyColumnName: 'C_Activity_ID',
          targetEntity: 'MaterialMgmtABCActivity'
        }, {
          autoExpand: true,
          type: '_id_130',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'AD_OrgTrx_ID',
            inpColumnName: 'inpadOrgtrxId',
            referencedKeyColumnName: 'AD_Org_ID',
            targetEntity: 'Organization',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.$Element_OT === 'Y');
            }
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'trxOrganization._identifier',
          valueField: 'trxOrganization',
          foreignKeyField: true,
          name: 'trxOrganization',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'trxOrganization')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Trx Organization',
          prompt: 'Trx Organization',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'AD_OrgTrx_ID',
          inpColumnName: 'inpadOrgtrxId',
          referencedKeyColumnName: 'AD_Org_ID',
          targetEntity: 'Organization'
        }, {
          autoExpand: true,
          type: '_id_10',
          editorProperties: {
            width: '*',
            columnName: 'User2_ID',
            inpColumnName: 'inpuser2Id',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.$Element_U2 === 'Y');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'ndDimension')])",
          width: isc.OBGrid.getDefaultColumnWidth(44),
          name: 'ndDimension',
          canExport: true,
          canHide: true,
          editorType: 'OBTextItem',
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextFilterItem',
          title: '2nd Dimension',
          prompt: '2nd Dimension',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'User2_ID',
          inpColumnName: 'inpuser2Id',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_10',
          editorProperties: {
            width: '*',
            columnName: 'User1_ID',
            inpColumnName: 'inpuser1Id',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true,
            showIf: function (item, value, form, currentValues) {
              currentValues = currentValues || form.view.getCurrentValues();
              var context = form.view.getContextInfo(false, true);
              return context && (context.$Element_U1 === 'Y');
            }
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'stDimension')])",
          width: isc.OBGrid.getDefaultColumnWidth(44),
          name: 'stDimension',
          canExport: true,
          canHide: true,
          editorType: 'OBTextItem',
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextFilterItem',
          title: '1st Dimension',
          prompt: '1st Dimension',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'User1_ID',
          inpColumnName: 'inpuser1Id',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'AD_Client_ID',
            inpColumnName: 'inpadClientId',
            referencedKeyColumnName: 'AD_Client_ID',
            targetEntity: 'ADClient',
            disabled: false,
            readonly: false,
            updatable: false
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'client._identifier',
          valueField: 'client',
          foreignKeyField: true,
          name: 'client',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Client',
          prompt: 'Client',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'AD_Client_ID',
          inpColumnName: 'inpadClientId',
          referencedKeyColumnName: 'AD_Client_ID',
          targetEntity: 'ADClient'
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsActive',
            inpColumnName: 'inpisactive',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'active',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Active',
          prompt: 'Active',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsActive',
          inpColumnName: 'inpisactive',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsDelivered',
            inpColumnName: 'inpisdelivered',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: false
          },
          name: 'delivered',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Delivered',
          prompt: 'Delivered',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsDelivered',
          inpColumnName: 'inpisdelivered',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsInvoiced',
            inpColumnName: 'inpisinvoiced',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: false
          },
          name: 'reinvoice',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Re-invoice',
          prompt: 'Re-invoice',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsInvoiced',
          inpColumnName: 'inpisinvoiced',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsPrinted',
            inpColumnName: 'inpisprinted',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: false
          },
          name: 'print',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Print',
          prompt: 'Print',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsPrinted',
          inpColumnName: 'inpisprinted',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_15',
          cellAlign: 'left',
          editorProperties: {
            "width": "50%",
            columnName: 'DateAcct',
            inpColumnName: 'inpdateacct',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'accountingDate',
          canExport: true,
          canHide: true,
          editorType: 'OBDateItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBMiniDateRangeItem',
          title: 'Accounting Date',
          prompt: 'Accounting Date',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DateAcct',
          inpColumnName: 'inpdateacct',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'Processed',
            inpColumnName: 'inpprocessed',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: false,
            redrawOnChange: true,
            changed: function (form, item, value) {
              if (this.pickValue && !this._pickedValue) {
                return;
              }
              this.Super('changed', arguments);
              form.onFieldChanged(form, item, value);
              form.view.toolBar.refreshCustomButtonsView(form.view);
            }
          },
          name: 'processed',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Processed',
          prompt: 'Processed',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'Processed',
          inpColumnName: 'inpprocessed',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_15',
          cellAlign: 'left',
          editorProperties: {
            "width": "50%",
            columnName: 'DatePrinted',
            inpColumnName: 'inpdateprinted',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'datePrinted',
          canExport: true,
          canHide: true,
          editorType: 'OBDateItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBMiniDateRangeItem',
          title: 'Date printed',
          prompt: 'Date printed',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DatePrinted',
          inpColumnName: 'inpdateprinted',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsSOTrx',
            inpColumnName: 'inpissotrx',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'salesTransaction',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Sales Transaction',
          prompt: 'Sales Transaction',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsSOTrx',
          inpColumnName: 'inpissotrx',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_195',
          editorProperties: {
            width: '',
            columnName: 'PaymentRule',
            inpColumnName: 'inppaymentrule',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'formOfPayment')])",
          width: isc.OBGrid.getDefaultColumnWidth(21),
          name: 'formOfPayment',
          canExport: true,
          canHide: true,
          editorType: 'OBListItem',
          filterOnKeypress: false,
          canFilter: true,
          filterEditorType: 'OBListFilterItem',
          title: 'Form of Payment',
          prompt: 'Form of Payment',
          required: true,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'PaymentRule',
          inpColumnName: 'inppaymentrule',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsSelected',
            inpColumnName: 'inpisselected',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          name: 'selected',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Selected',
          prompt: 'Selected',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsSelected',
          inpColumnName: 'inpisselected',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_110',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'DropShip_User_ID',
            inpColumnName: 'inpdropshipUserId',
            referencedKeyColumnName: 'AD_User_ID',
            targetEntity: 'ADUser',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'dropShipContact._identifier',
          valueField: 'dropShipContact',
          foreignKeyField: true,
          name: 'dropShipContact',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'dropShipContact')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Drop Ship Contact',
          prompt: 'Drop Ship Contact',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DropShip_User_ID',
          inpColumnName: 'inpdropshipUserId',
          referencedKeyColumnName: 'AD_User_ID',
          targetEntity: 'ADUser'
        }, {
          autoExpand: true,
          type: '_id_173',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'DropShip_BPartner_ID',
            inpColumnName: 'inpdropshipBpartnerId',
            referencedKeyColumnName: 'C_BPartner_ID',
            targetEntity: 'BusinessPartner',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'dropShipPartner._identifier',
          valueField: 'dropShipPartner',
          foreignKeyField: true,
          name: 'dropShipPartner',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'dropShipPartner')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Drop Ship Partner',
          prompt: 'Drop Ship Partner',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DropShip_BPartner_ID',
          inpColumnName: 'inpdropshipBpartnerId',
          referencedKeyColumnName: 'C_BPartner_ID',
          targetEntity: 'BusinessPartner'
        }, {
          autoExpand: true,
          type: '_id_159',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'DropShip_Location_ID',
            inpColumnName: 'inpdropshipLocationId',
            referencedKeyColumnName: 'C_BPartner_Location_ID',
            targetEntity: 'BusinessPartnerLocation',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'dropShipLocation._identifier',
          valueField: 'dropShipLocation',
          foreignKeyField: true,
          name: 'dropShipLocation',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'dropShipLocation')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Drop Ship Location',
          prompt: 'Drop Ship Location',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'DropShip_Location_ID',
          inpColumnName: 'inpdropshipLocationId',
          referencedKeyColumnName: 'C_BPartner_Location_ID',
          targetEntity: 'BusinessPartnerLocation'
        }, {
          autoExpand: false,
          type: '_id_20',
          editorProperties: {
            "width": 1,
            "overflow": "visible",
            "showTitle": false,
            "showLabel": false,
            columnName: 'IsSelfService',
            inpColumnName: 'inpisselfservice',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: true,
            readonly: true,
            updatable: true
          },
          name: 'selfService',
          canExport: true,
          canHide: true,
          editorType: 'OBCheckboxItem',
          width: '*',
          autoFitWidth: false,
          formatCellValue: function (value, record, rowNum, colNum, grid) {
            return OB.Utilities.getYesNoDisplayValue(value);
          },
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBYesNoItem',
          title: 'Self-Service',
          prompt: 'Self-Service',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'IsSelfService',
          inpColumnName: 'inpisselfservice',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_14',
          editorProperties: {
            width: '',
            columnName: 'Deliverynotes',
            inpColumnName: 'inpdeliverynotes',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'deliveryNotes')])",
          width: isc.OBGrid.getDefaultColumnWidth(2000),
          name: 'deliveryNotes',
          canExport: true,
          canHide: true,
          editorType: 'OBPopUpTextAreaItem',
          canSort: false,
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextItem',
          title: 'Delivery notes',
          prompt: 'Delivery notes',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'Deliverynotes',
          inpColumnName: 'inpdeliverynotes',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: true,
          type: '_id_19',
          editorProperties: {
            displayField: null,
            valueField: null,
            columnName: 'C_Incoterms_ID',
            inpColumnName: 'inpcIncotermsId',
            referencedKeyColumnName: 'C_Incoterms_ID',
            targetEntity: 'FinancialMgmtIncoterms',
            disabled: false,
            readonly: false,
            updatable: true
          },
          width: isc.OBGrid.getDefaultColumnWidth(44),
          displayField: 'incoterms._identifier',
          valueField: 'incoterms',
          foreignKeyField: true,
          name: 'incoterms',
          canExport: true,
          canHide: true,
          editorType: 'OBFKItem',
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'incoterms')])",
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBFKFilterTextItem',
          title: 'Incoterms',
          prompt: 'Incoterms',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'C_Incoterms_ID',
          inpColumnName: 'inpcIncotermsId',
          referencedKeyColumnName: 'C_Incoterms_ID',
          targetEntity: 'FinancialMgmtIncoterms'
        }, {
          autoExpand: true,
          type: '_id_14',
          editorProperties: {
            width: '',
            columnName: 'Incotermsdescription',
            inpColumnName: 'inpincotermsdescription',
            referencedKeyColumnName: '',
            targetEntity: '',
            disabled: false,
            readonly: false,
            updatable: true
          },
          showHover: true,
          hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'iNCOTERMSDescription')])",
          width: isc.OBGrid.getDefaultColumnWidth(255),
          name: 'iNCOTERMSDescription',
          canExport: true,
          canHide: true,
          editorType: 'OBPopUpTextAreaItem',
          canSort: false,
          filterOnKeypress: true,
          canFilter: true,
          filterEditorType: 'OBTextItem',
          title: 'INCOTERMS description',
          prompt: 'INCOTERMS description',
          required: false,
          escapeHTML: true,
          showIf: 'false',
          columnName: 'Incotermsdescription',
          inpColumnName: 'inpincotermsdescription',
          referencedKeyColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_16',
          editorProperties: {
            width: '*',
            columnName: 'creationDate',
            targetEntity: '',
            disabled: true,
            updatable: false
          },
          showHover: false,
          width: isc.OBGrid.getDefaultColumnWidth(30),
          name: 'creationDate',
          canExport: true,
          canHide: true,
          editorType: 'OBDateItem',
          filterEditorType: 'OBMiniDateRangeItem',
          filterOnKeypress: true,
          canFilter: true,
          required: false,
          title: 'Creation Date',
          prompt: 'Creation Date',
          escapeHTML: true,
          showIf: 'false',
          columnName: 'creationDate',
          inpColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_30',
          editorProperties: {
            width: '*',
            columnName: 'createdBy',
            targetEntity: 'User',
            disabled: true,
            updatable: false
          },
          showHover: false,
          width: isc.OBGrid.getDefaultColumnWidth(30),
          name: 'createdBy',
          canExport: true,
          canHide: true,
          editorType: 'OBSearchItem',
          filterEditorType: 'OBFKFilterTextItem',
          displayField: 'createdBy._identifier',
          valueField: 'createdBy',
          filterOnKeypress: true,
          canFilter: true,
          required: false,
          title: 'Created By',
          prompt: 'Created By',
          escapeHTML: true,
          showIf: 'false',
          columnName: 'createdBy',
          inpColumnName: '',
          targetEntity: 'User'
        }, {
          autoExpand: false,
          type: '_id_16',
          editorProperties: {
            width: '*',
            columnName: 'updated',
            targetEntity: '',
            disabled: true,
            updatable: false
          },
          showHover: false,
          width: isc.OBGrid.getDefaultColumnWidth(30),
          name: 'updated',
          canExport: true,
          canHide: true,
          editorType: 'OBDateItem',
          filterEditorType: 'OBMiniDateRangeItem',
          filterOnKeypress: true,
          canFilter: true,
          required: false,
          title: 'Updated',
          prompt: 'Updated',
          escapeHTML: true,
          showIf: 'false',
          columnName: 'updated',
          inpColumnName: '',
          targetEntity: ''
        }, {
          autoExpand: false,
          type: '_id_30',
          editorProperties: {
            width: '*',
            columnName: 'updatedBy',
            targetEntity: 'User',
            disabled: true,
            updatable: false
          },
          showHover: false,
          width: isc.OBGrid.getDefaultColumnWidth(30),
          name: 'updatedBy',
          canExport: true,
          canHide: true,
          editorType: 'OBSearchItem',
          filterEditorType: 'OBFKFilterTextItem',
          displayField: 'updatedBy._identifier',
          valueField: 'updatedBy',
          filterOnKeypress: true,
          canFilter: true,
          required: false,
          title: 'Updated By',
          prompt: 'Updated By',
          escapeHTML: true,
          showIf: 'false',
          columnName: 'updatedBy',
          inpColumnName: '',
          targetEntity: 'User'
        }],
        autoExpandFieldNames: ['deliveryNotes', 'iNCOTERMSDescription', 'description', 'organization', 'transactionDocument', 'businessPartner', 'partnerAddress', 'currency', 'priceList', 'paymentTerms', 'warehouse', 'userContact', 'project', 'documentType', 'salesRepresentative', 'invoiceAddress', 'deliveryLocation', 'shippingCompany', 'salesCampaign', 'charge', 'activity', 'trxOrganization', 'ndDimension', 'stDimension', 'client', 'dropShipContact', 'dropShipPartner', 'dropShipLocation', 'incoterms', 'paymentMethod', 'orderReference'],
        whereClause: 'e.salesTransaction=true',
        orderByClause: '',
        sortField: 'documentNo',
        filterClause: ' ( e.updated > @transactionalRange@  or e.processed = \'N\' ) ',
        filterName: 'This grid is filtered using transactional filter <i>(only draft & modified documents in the last 1 day(s))</i>.',
        foreignKeyFieldNames: ['organization', 'transactionDocument', 'businessPartner', 'partnerAddress', 'currency', 'priceList', 'paymentMethod', 'paymentTerms', 'warehouse', 'userContact', 'project', 'documentType', 'salesRepresentative', 'invoiceAddress', 'deliveryLocation', 'shippingCompany', 'salesCampaign', 'charge', 'activity', 'trxOrganization', 'client', 'dropShipContact', 'dropShipPartner', 'dropShipLocation', 'incoterms']
      });
      this.Super('initWidget', arguments);
    },
    createViewStructure: function () {
      this.addChildView(isc.OBStandardView.create({
        tabTitle: 'Lines',
        entity: 'OrderLine',
        parentProperty: 'salesOrder',
        tabId: '187',
        moduleId: '0',
        defaultEditMode: false,
        mapping250: '/SalesOrder/Lines',
        isAcctTab: false,
        isTrlTab: false,
        standardProperties: {
          inpTabId: '187',
          inpwindowId: '143',
          inpTableId: '260',
          inpkeyColumnId: 'C_OrderLine_ID',
          inpKeyName: 'inpcOrderlineId'
        },
        propertyToColumns: [{
          property: 'lineNo',
          inpColumn: 'inpline',
          dbColumn: 'Line',
          sessionProperty: false,
          type: '_id_11'
        }, {
          property: 'product',
          inpColumn: 'inpmProductId',
          dbColumn: 'M_Product_ID',
          sessionProperty: true,
          type: '_id_800060'
        }, {
          property: 'orderedQuantity',
          inpColumn: 'inpqtyordered',
          dbColumn: 'QtyOrdered',
          sessionProperty: false,
          type: '_id_29'
        }, {
          property: 'attributeSetValue',
          inpColumn: 'inpmAttributesetinstanceId',
          dbColumn: 'M_AttributeSetInstance_ID',
          sessionProperty: false,
          type: '_id_35'
        }, {
          property: 'uOM',
          inpColumn: 'inpcUomId',
          dbColumn: 'C_UOM_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'unitPrice',
          inpColumn: 'inppriceactual',
          dbColumn: 'PriceActual',
          sessionProperty: false,
          type: '_id_800008'
        }, {
          property: 'lineNetAmount',
          inpColumn: 'inplinenetamt',
          dbColumn: 'LineNetAmt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'tax',
          inpColumn: 'inpcTaxId',
          dbColumn: 'C_Tax_ID',
          sessionProperty: false,
          type: '_id_158'
        }, {
          property: 'listPrice',
          inpColumn: 'inppricelist',
          dbColumn: 'PriceList',
          sessionProperty: false,
          type: '_id_800008'
        }, {
          property: 'discount',
          inpColumn: 'inpdiscount',
          dbColumn: 'Discount',
          sessionProperty: false,
          type: '_id_22'
        }, {
          property: 'description',
          inpColumn: 'inpdescription',
          dbColumn: 'Description',
          sessionProperty: false,
          type: '_id_14'
        }, {
          property: 'taxableAmount',
          inpColumn: 'inptaxbaseamt',
          dbColumn: 'Taxbaseamt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'invoicedQuantity',
          inpColumn: 'inpqtyinvoiced',
          dbColumn: 'QtyInvoiced',
          sessionProperty: false,
          type: '_id_29'
        }, {
          property: 'deliveredQuantity',
          inpColumn: 'inpqtydelivered',
          dbColumn: 'QtyDelivered',
          sessionProperty: false,
          type: '_id_29'
        }, {
          property: 'orderDate',
          inpColumn: 'inpdateordered',
          dbColumn: 'DateOrdered',
          sessionProperty: true,
          type: '_id_15'
        }, {
          property: 'scheduledDeliveryDate',
          inpColumn: 'inpdatepromised',
          dbColumn: 'DatePromised',
          sessionProperty: true,
          type: '_id_15'
        }, {
          property: 'warehouse',
          inpColumn: 'inpmWarehouseId',
          dbColumn: 'M_Warehouse_ID',
          sessionProperty: true,
          type: '_id_197'
        }, {
          property: 'reservedQuantity',
          inpColumn: 'inpqtyreserved',
          dbColumn: 'QtyReserved',
          sessionProperty: false,
          type: '_id_29'
        }, {
          property: 'shippingCompany',
          inpColumn: 'inpmShipperId',
          dbColumn: 'M_Shipper_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'businessPartner',
          inpColumn: 'inpcBpartnerId',
          dbColumn: 'C_BPartner_ID',
          sessionProperty: false,
          type: '_id_800057'
        }, {
          property: 'directShipment',
          inpColumn: 'inpdirectship',
          dbColumn: 'DirectShip',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'freightAmount',
          inpColumn: 'inpfreightamt',
          dbColumn: 'FreightAmt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'partnerAddress',
          inpColumn: 'inpcBpartnerLocationId',
          dbColumn: 'C_BPartner_Location_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'cancelPriceAdjustment',
          inpColumn: 'inpcancelpricead',
          dbColumn: 'CANCELPRICEAD',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'orderUOM',
          inpColumn: 'inpmProductUomId',
          dbColumn: 'M_Product_Uom_Id',
          sessionProperty: false,
          type: '_id_800000'
        }, {
          property: 'orderQuantity',
          inpColumn: 'inpquantityorder',
          dbColumn: 'QuantityOrder',
          sessionProperty: false,
          type: '_id_29'
        }, {
          property: 'standardPrice',
          inpColumn: 'inppricestd',
          dbColumn: 'PriceStd',
          sessionProperty: false,
          type: '_id_800008'
        }, {
          property: 'editLineAmount',
          inpColumn: 'inpiseditlinenetamt',
          dbColumn: 'Iseditlinenetamt',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'priceAdjustment',
          inpColumn: 'inpmOfferId',
          dbColumn: 'M_Offer_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'orderDiscount',
          inpColumn: 'inpcOrderDiscountId',
          dbColumn: 'C_Order_Discount_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'descriptionOnly',
          inpColumn: 'inpisdescription',
          dbColumn: 'IsDescription',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'sOPOReference',
          inpColumn: 'inprefOrderlineId',
          dbColumn: 'Ref_OrderLine_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'resourceAssignment',
          inpColumn: 'inpsResourceassignmentId',
          dbColumn: 'S_ResourceAssignment_ID',
          sessionProperty: false,
          type: '_id_33'
        }, {
          property: 'priceLimit',
          inpColumn: 'inppricelimit',
          dbColumn: 'PriceLimit',
          sessionProperty: false,
          type: '_id_800008'
        }, {
          property: 'charge',
          inpColumn: 'inpcChargeId',
          dbColumn: 'C_Charge_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'chargeAmount',
          inpColumn: 'inpchargeamt',
          dbColumn: 'ChargeAmt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'organization',
          inpColumn: 'inpadOrgId',
          dbColumn: 'AD_Org_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'currency',
          inpColumn: 'inpcCurrencyId',
          dbColumn: 'C_Currency_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'dateDelivered',
          inpColumn: 'inpdatedelivered',
          dbColumn: 'DateDelivered',
          sessionProperty: false,
          type: '_id_15'
        }, {
          property: 'salesOrder',
          inpColumn: 'inpcOrderId',
          dbColumn: 'C_Order_ID',
          sessionProperty: true,
          type: '_id_800062'
        }, {
          property: 'active',
          inpColumn: 'inpisactive',
          dbColumn: 'IsActive',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'client',
          inpColumn: 'inpadClientId',
          dbColumn: 'AD_Client_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'id',
          inpColumn: 'inpcOrderlineId',
          dbColumn: 'C_OrderLine_ID',
          sessionProperty: false,
          type: '_id_13'
        }, {
          property: 'invoiceDate',
          inpColumn: 'inpdateinvoiced',
          dbColumn: 'DateInvoiced',
          sessionProperty: false,
          type: '_id_15'
        }, {
          property: 'id',
          inpColumn: 'C_OrderLine_ID',
          dbColumn: 'C_OrderLine_ID',
          sessionProperty: true,
          type: '_id_13'
        }],
        actionToolbarButtons: [],
        showParentButtons: true,
        buttonsHaveSessionLogic: false,
        iconToolbarButtons: [],
        hasChildTabs: true,
        initWidget: function () {
          this.dataSource = OB.Datasource.create({
            createClassName: 'OBViewDataSource',
            titleField: OB.Constants.IDENTIFIER,
            dataURL: '/openbravo/org.openbravo.service.datasource/OrderLine',
            recordXPath: '/response/data',
            dataFormat: 'json',
            operationBindings: [{
              operationType: 'fetch',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'POST'
              }
            }, {
              operationType: 'add',
              dataProtocol: 'postMessage'
            }, {
              operationType: 'remove',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'DELETE'
              }
            }, {
              operationType: 'update',
              dataProtocol: 'postMessage',
              requestProperties: {
                httpMethod: 'PUT'
              }
            }],
            requestProperties: {
              params: {
                _className: 'OBViewDataSource'
              }
            },
            fields: [{
              name: 'id',
              type: '_id_13',
              additional: false,
              hidden: true,
              primaryKey: true,
              canSave: false,
              title: 'id'
            }, {
              name: 'client',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'client',
              hidden: true
            }, {
              name: 'client._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'client'
            }, {
              name: 'organization',
              type: '_id_19',
              additional: false,
              required: true,
              title: 'organization',
              hidden: true
            }, {
              name: 'organization._identifier',
              type: 'text',
              hidden: true,
              title: 'organization'
            }, {
              name: 'active',
              type: '_id_20',
              additional: false,
              title: 'active'
            }, {
              name: 'creationDate',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'creationDate'
            }, {
              name: 'createdBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'createdBy',
              hidden: true
            }, {
              name: 'createdBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'createdBy'
            }, {
              name: 'updated',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'updated'
            }, {
              name: 'updatedBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'updatedBy',
              hidden: true
            }, {
              name: 'updatedBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'updatedBy'
            }, {
              name: 'salesOrder',
              type: '_id_800062',
              additional: false,
              canSave: false,
              title: 'salesOrder',
              hidden: true
            }, {
              name: 'salesOrder._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'salesOrder'
            }, {
              name: 'lineNo',
              type: '_id_11',
              additional: false,
              required: true,
              title: 'lineNo'
            }, {
              name: 'businessPartner',
              type: '_id_800057',
              additional: false,
              title: 'businessPartner',
              hidden: true
            }, {
              name: 'businessPartner._identifier',
              type: 'text',
              hidden: true,
              title: 'businessPartner'
            }, {
              name: 'partnerAddress',
              type: '_id_19',
              additional: false,
              title: 'partnerAddress',
              hidden: true
            }, {
              name: 'partnerAddress._identifier',
              type: 'text',
              hidden: true,
              title: 'partnerAddress'
            }, {
              name: 'orderDate',
              type: '_id_15',
              additional: false,
              required: true,
              title: 'orderDate'
            }, {
              name: 'scheduledDeliveryDate',
              type: '_id_15',
              additional: false,
              title: 'scheduledDeliveryDate'
            }, {
              name: 'dateDelivered',
              type: '_id_15',
              additional: false,
              canSave: false,
              title: 'dateDelivered'
            }, {
              name: 'invoiceDate',
              type: '_id_15',
              additional: false,
              canSave: false,
              title: 'invoiceDate'
            }, {
              name: 'description',
              type: '_id_14',
              additional: false,
              length: 2000,
              title: 'description'
            }, {
              name: 'product',
              type: '_id_800060',
              additional: false,
              title: 'product',
              hidden: true
            }, {
              name: 'product._identifier',
              type: 'text',
              hidden: true,
              title: 'product'
            }, {
              name: 'warehouse',
              type: '_id_197',
              additional: false,
              required: true,
              title: 'warehouse',
              hidden: true
            }, {
              name: 'warehouse._identifier',
              type: 'text',
              hidden: true,
              title: 'warehouse'
            }, {
              name: 'directShipment',
              type: '_id_20',
              additional: false,
              canSave: false,
              title: 'directShipment'
            }, {
              name: 'uOM',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'uOM',
              hidden: true
            }, {
              name: 'uOM._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'uOM'
            }, {
              name: 'orderedQuantity',
              type: '_id_29',
              additional: false,
              required: true,
              title: 'orderedQuantity'
            }, {
              name: 'reservedQuantity',
              type: '_id_29',
              additional: false,
              canSave: false,
              title: 'reservedQuantity'
            }, {
              name: 'deliveredQuantity',
              type: '_id_29',
              additional: false,
              canSave: false,
              title: 'deliveredQuantity'
            }, {
              name: 'invoicedQuantity',
              type: '_id_29',
              additional: false,
              canSave: false,
              title: 'invoicedQuantity'
            }, {
              name: 'shippingCompany',
              type: '_id_19',
              additional: false,
              title: 'shippingCompany',
              hidden: true
            }, {
              name: 'shippingCompany._identifier',
              type: 'text',
              hidden: true,
              title: 'shippingCompany'
            }, {
              name: 'currency',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'currency',
              hidden: true
            }, {
              name: 'currency._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'currency'
            }, {
              name: 'listPrice',
              type: '_id_800008',
              additional: false,
              required: true,
              title: 'listPrice'
            }, {
              name: 'unitPrice',
              type: '_id_800008',
              additional: false,
              required: true,
              title: 'unitPrice'
            }, {
              name: 'priceLimit',
              type: '_id_800008',
              additional: false,
              required: true,
              title: 'priceLimit'
            }, {
              name: 'lineNetAmount',
              type: '_id_12',
              additional: false,
              required: true,
              title: 'lineNetAmount'
            }, {
              name: 'discount',
              type: '_id_22',
              additional: false,
              title: 'discount'
            }, {
              name: 'freightAmount',
              type: '_id_12',
              additional: false,
              required: true,
              title: 'freightAmount'
            }, {
              name: 'charge',
              type: '_id_19',
              additional: false,
              title: 'charge',
              hidden: true
            }, {
              name: 'charge._identifier',
              type: 'text',
              hidden: true,
              title: 'charge'
            }, {
              name: 'chargeAmount',
              type: '_id_12',
              additional: false,
              title: 'chargeAmount'
            }, {
              name: 'tax',
              type: '_id_158',
              additional: false,
              required: true,
              title: 'tax',
              hidden: true
            }, {
              name: 'tax._identifier',
              type: 'text',
              hidden: true,
              title: 'tax'
            }, {
              name: 'resourceAssignment',
              type: '_id_33',
              additional: false,
              title: 'resourceAssignment',
              hidden: true
            }, {
              name: 'resourceAssignment._identifier',
              type: 'text',
              hidden: true,
              title: 'resourceAssignment'
            }, {
              name: 'sOPOReference',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'sOPOReference',
              hidden: true
            }, {
              name: 'sOPOReference._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'sOPOReference'
            }, {
              name: 'attributeSetValue',
              type: '_id_35',
              additional: false,
              title: 'attributeSetValue',
              hidden: true
            }, {
              name: 'attributeSetValue._identifier',
              type: 'text',
              hidden: true,
              title: 'attributeSetValue'
            }, {
              name: 'descriptionOnly',
              type: '_id_20',
              additional: false,
              title: 'descriptionOnly'
            }, {
              name: 'orderQuantity',
              type: '_id_29',
              additional: false,
              title: 'orderQuantity'
            }, {
              name: 'orderUOM',
              type: '_id_800000',
              additional: false,
              title: 'orderUOM',
              hidden: true
            }, {
              name: 'orderUOM._identifier',
              type: 'text',
              hidden: true,
              title: 'orderUOM'
            }, {
              name: 'priceAdjustment',
              type: '_id_19',
              additional: false,
              title: 'priceAdjustment',
              hidden: true
            }, {
              name: 'priceAdjustment._identifier',
              type: 'text',
              hidden: true,
              title: 'priceAdjustment'
            }, {
              name: 'standardPrice',
              type: '_id_800008',
              additional: false,
              required: true,
              title: 'standardPrice'
            }, {
              name: 'cancelPriceAdjustment',
              type: '_id_20',
              additional: false,
              title: 'cancelPriceAdjustment'
            }, {
              name: 'orderDiscount',
              type: '_id_19',
              additional: false,
              title: 'orderDiscount',
              hidden: true
            }, {
              name: 'orderDiscount._identifier',
              type: 'text',
              hidden: true,
              title: 'orderDiscount'
            }, {
              name: 'taxableAmount',
              type: '_id_12',
              additional: false,
              title: 'taxableAmount'
            }, {
              name: 'editLineAmount',
              type: '_id_20',
              additional: false,
              title: 'editLineAmount'
            }, {
              name: 'businessPartner.name',
              type: '_id_10',
              additional: true,
              required: true,
              length: 60,
              title: 'businessPartner.name'
            }]
          });
          this.viewForm = isc.OBViewForm.create({
            fields: [{
              name: 'lineNo',
              title: 'Line No.',
              type: '_id_11',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Line',
              inpColumnName: 'inpline',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'product',
              title: 'Product',
              type: '_id_800060',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'M_Product_ID',
              inpColumnName: 'inpmProductId',
              referencedKeyColumnName: 'M_Product_ID',
              targetEntity: 'Product',
              required: true,
              firstFocusedField: true,
              selectorDefinitionId: '2E64F551C7C4470C80C29DBA24B34A5F',
              popupTextMatchStyle: 'startsWith',
              textMatchStyle: 'startsWith',
              defaultPopupFilterField: '_identifier',
              displayField: '_identifier',
              valueField: 'product.id',
              pickListFields: [{
                title: ' ',
                name: '_identifier',
                disableFilter: true,
                canSort: false,
                type: 'text'
              }],
              showSelectorGrid: true,
              selectorGridFields: [{
                title: 'Search Key',
                name: 'product.searchKey',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Name',
                name: 'product.name',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Available',
                name: 'available',
                disableFilter: false,
                canSort: true,
                type: '_id_29',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Warehouse',
                name: 'warehouse',
                disableFilter: false,
                canSort: true,
                type: '_id_19',
                displayField: 'warehouse._identifier',
                filterOperator: 'equals',
                filterOnKeypress: true,
                canFilter: true,
                required: false,
                filterEditorType: 'OBSelectorFilterSelectItem',
                filterEditorProperties: {
                  entity: 'Warehouse',
                  displayField: '_identifier'
                }
              }, {
                title: 'Net Unit Price',
                name: 'standardPrice',
                disableFilter: false,
                canSort: true,
                type: '_id_800008',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Net List Price',
                name: 'netListPrice',
                disableFilter: false,
                canSort: true,
                type: '_id_800008',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Price List Version',
                name: 'productPrice.priceListVersion',
                disableFilter: false,
                canSort: true,
                type: '_id_19',
                displayField: 'productPrice.priceListVersion._identifier',
                filterOperator: 'equals',
                filterOnKeypress: true,
                canFilter: true,
                required: false,
                filterEditorType: 'OBSelectorFilterSelectItem',
                filterEditorProperties: {
                  entity: 'PricingPriceListVersion',
                  displayField: '_identifier'
                }
              }, {
                title: 'Warehouse Qty.',
                name: 'qtyOnHand',
                disableFilter: false,
                canSort: true,
                type: '_id_29',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Ordered Qty.',
                name: 'qtyOrdered',
                disableFilter: false,
                canSort: true,
                type: '_id_29',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Net Price Limit',
                name: 'priceLimit',
                disableFilter: false,
                canSort: true,
                type: '_id_800008',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }],
              outFields: {
                'id': {
                  'fieldName': '',
                  'suffix': ''
                },
                '_identifier': {
                  'fieldName': '',
                  'suffix': ''
                },
                'productPrice.priceListVersion.priceList.currency.id': {
                  'fieldName': 'productPrice.priceListVersion.priceList.currency.id',
                  'suffix': '_CURR'
                },
                'product.uOM.id': {
                  'fieldName': 'product.uOM.id',
                  'suffix': '_UOM'
                },
                'standardPrice': {
                  'fieldName': 'standardPrice',
                  'suffix': '_PSTD'
                },
                'netListPrice': {
                  'fieldName': 'netListPrice',
                  'suffix': '_PLIST'
                },
                'priceLimit': {
                  'fieldName': 'priceLimit',
                  'suffix': '_PLIM'
                }
              },
              extraSearchFields: ['product.name', 'product._identifier', 'product.searchKey'],
              optionDataSource: OB.Datasource.create({
                createClassName: '',
                titleField: OB.Constants.IDENTIFIER,
                dataURL: '/openbravo/org.openbravo.service.datasource/ProductByPriceAndWarehouse',
                recordXPath: '/response/data',
                dataFormat: 'json',
                operationBindings: [{
                  operationType: 'fetch',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'POST'
                  }
                }, {
                  operationType: 'add',
                  dataProtocol: 'postMessage'
                }, {
                  operationType: 'remove',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'DELETE'
                  }
                }, {
                  operationType: 'update',
                  dataProtocol: 'postMessage',
                  requestProperties: {
                    httpMethod: 'PUT'
                  }
                }],
                requestProperties: {
                  params: {
                    targetProperty: 'product',
                    adTabId: '187',
                    IsSelectorItem: 'true',
                    columnName: 'M_Product_ID',
                    _extraProperties: 'product.id,productPrice.priceListVersion._identifier,available,warehouse._identifier,productPrice.priceListVersion.priceList.currency.id,product.name,qtyOnHand,product._identifier,qtyOrdered,netListPrice,product.uOM.id,priceLimit,standardPrice,product.searchKey'
                  }
                },
                fields: [{
                  name: 'id',
                  type: '_id_13',
                  additional: false,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'id'
                }, {
                  name: 'client',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'client',
                  hidden: true
                }, {
                  name: 'client._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'client'
                }, {
                  name: 'organization',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'organization',
                  hidden: true
                }, {
                  name: 'organization._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'organization'
                }, {
                  name: 'active',
                  type: '_id_20',
                  additional: false,
                  title: 'active'
                }, {
                  name: 'updated',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'updated'
                }, {
                  name: 'updatedBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'updatedBy',
                  hidden: true
                }, {
                  name: 'updatedBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'updatedBy'
                }, {
                  name: 'creationDate',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'creationDate'
                }, {
                  name: 'createdBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'createdBy',
                  hidden: true
                }, {
                  name: 'createdBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'createdBy'
                }, {
                  name: 'product',
                  type: '_id_800060',
                  additional: false,
                  required: true,
                  title: 'product',
                  hidden: true
                }, {
                  name: 'product._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'product'
                }, {
                  name: 'warehouse',
                  type: '_id_19',
                  additional: false,
                  required: true,
                  title: 'warehouse',
                  hidden: true
                }, {
                  name: 'warehouse._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'warehouse'
                }, {
                  name: 'productPrice',
                  type: '_id_19',
                  additional: false,
                  required: true,
                  title: 'productPrice',
                  hidden: true
                }, {
                  name: 'productPrice._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'productPrice'
                }, {
                  name: 'available',
                  type: '_id_29',
                  additional: false,
                  title: 'available'
                }, {
                  name: 'qtyOnHand',
                  type: '_id_29',
                  additional: false,
                  title: 'qtyOnHand'
                }, {
                  name: 'qtyReserved',
                  type: '_id_29',
                  additional: false,
                  title: 'qtyReserved'
                }, {
                  name: 'qtyOrdered',
                  type: '_id_29',
                  additional: false,
                  title: 'qtyOrdered'
                }, {
                  name: 'netListPrice',
                  type: '_id_800008',
                  additional: false,
                  title: 'netListPrice'
                }, {
                  name: 'standardPrice',
                  type: '_id_800008',
                  additional: false,
                  title: 'standardPrice'
                }, {
                  name: 'priceLimit',
                  type: '_id_800008',
                  additional: false,
                  title: 'priceLimit'
                }, {
                  name: 'product.id',
                  type: '_id_13',
                  additional: true,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'product.id'
                }, {
                  name: 'productPrice.priceListVersion._identifier',
                  type: '_id_10',
                  additional: true,
                  required: true,
                  length: 60,
                  title: 'productPrice.priceListVersion._identifier'
                }, {
                  name: 'available',
                  type: '_id_29',
                  additional: true,
                  title: 'available'
                }, {
                  name: 'warehouse._identifier',
                  type: '_id_10',
                  additional: true,
                  required: true,
                  length: 60,
                  title: 'warehouse._identifier'
                }, {
                  name: 'productPrice.priceListVersion.priceList.currency.id',
                  type: '_id_13',
                  additional: true,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'productPrice.priceListVersion.priceList.currency.id'
                }, {
                  name: 'product.name',
                  type: '_id_10',
                  additional: true,
                  required: true,
                  length: 60,
                  title: 'product.name'
                }, {
                  name: 'qtyOnHand',
                  type: '_id_29',
                  additional: true,
                  title: 'qtyOnHand'
                }, {
                  name: 'product._identifier',
                  type: '_id_10',
                  additional: true,
                  required: true,
                  length: 60,
                  title: 'product._identifier'
                }, {
                  name: 'qtyOrdered',
                  type: '_id_29',
                  additional: true,
                  title: 'qtyOrdered'
                }, {
                  name: 'netListPrice',
                  type: '_id_800008',
                  additional: true,
                  title: 'netListPrice'
                }, {
                  name: 'product.uOM.id',
                  type: '_id_13',
                  additional: true,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'product.uOM.id'
                }, {
                  name: 'priceLimit',
                  type: '_id_800008',
                  additional: true,
                  title: 'priceLimit'
                }, {
                  name: 'standardPrice',
                  type: '_id_800008',
                  additional: true,
                  title: 'standardPrice'
                }, {
                  name: 'product.searchKey',
                  type: '_id_10',
                  additional: true,
                  required: true,
                  length: 40,
                  title: 'product.searchKey'
                }]
              }),
              whereClause: '',
              outHiddenInputPrefix: 'inpmProductId',
              width: '*',
              dummy: ''
            }, {
              name: 'orderedQuantity',
              title: 'Ordered Quantity',
              type: '_id_29',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'QtyOrdered',
              inpColumnName: 'inpqtyordered',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'attributeSetValue',
              title: 'Attribute Set Value',
              type: '_id_35',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'M_AttributeSetInstance_ID',
              inpColumnName: 'inpmAttributesetinstanceId',
              referencedKeyColumnName: 'M_AttributeSetInstance_ID',
              targetEntity: 'AttributeSetInstance',
              required: false,
              redrawOnChange: true,
              changed: function (form, item, value) {
                if (this.pickValue && !this._pickedValue) {
                  return;
                }
                this.Super('changed', arguments);
                form.onFieldChanged(form, item, value);
                form.view.toolBar.refreshCustomButtonsView(form.view);
              },
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && ((context.ATTRIBUTESET !== '' && context.ATTRSETVALUETYPE !== 'F') || (currentValues.attributeSetValue !== '' && currentValues.attributeSetValue !== '0'));
              },
              displayField: 'attributeSetValue._identifier',
              valueField: 'attributeSetValue',
              showPickerIcon: true,
              width: '*',
              dummy: ''
            }, {
              name: 'uOM',
              title: 'UOM',
              type: '_id_19',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_UOM_ID',
              inpColumnName: 'inpcUomId',
              referencedKeyColumnName: 'C_UOM_ID',
              targetEntity: 'UOM',
              required: true,
              width: '*',
              dummy: ''
            }, {
              name: 'unitPrice',
              title: 'Net Unit Price',
              type: '_id_800008',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'PriceActual',
              inpColumnName: 'inppriceactual',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'lineNetAmount',
              title: 'Line Net Amount',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'LineNetAmt',
              inpColumnName: 'inplinenetamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'tax',
              title: 'Tax',
              type: '_id_158',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Tax_ID',
              inpColumnName: 'inpcTaxId',
              referencedKeyColumnName: 'C_Tax_ID',
              targetEntity: 'FinancialMgmtTaxRate',
              required: true,
              width: '*',
              dummy: ''
            }, {
              name: 'listPrice',
              title: 'Net List Price',
              type: '_id_800008',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'PriceList',
              inpColumnName: 'inppricelist',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'discount',
              title: 'Discount',
              type: '_id_22',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Discount',
              inpColumnName: 'inpdiscount',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": "50%",
              dummy: ''
            }, {
              name: 'description',
              title: 'Description',
              type: '_id_14',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 2,
              rowSpan: 2,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Description',
              inpColumnName: 'inpdescription',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              width: '*',
              dummy: ''
            }, {
              name: '402880E72F1C15A5012F1C7AA98B00E8',
              title: 'More Information',
              type: 'OBSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: true,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'More Information',
              itemIds: ['taxableAmount', 'orderDate', 'scheduledDeliveryDate', 'warehouse', 'reservedQuantity', 'shippingCompany', 'businessPartner', 'directShipment', 'freightAmount', 'partnerAddress', 'cancelPriceAdjustment', 'orderUOM', 'orderQuantity', 'standardPrice', 'editLineAmount'],
              dummy: ''
            }, {
              name: 'taxableAmount',
              title: 'Alternate Taxable Amount',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Taxbaseamt',
              inpColumnName: 'inptaxbaseamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.UsesAlternate === 'Y');
              },
              "width": "50%",
              dummy: ''
            }, {
              name: 'orderDate',
              title: 'Order Date',
              type: '_id_15',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'DateOrdered',
              inpColumnName: 'inpdateordered',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP');
              },
              "width": "50%",
              dummy: ''
            }, {
              name: 'scheduledDeliveryDate',
              title: 'Scheduled Delivery Date',
              type: '_id_15',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'DatePromised',
              inpColumnName: 'inpdatepromised',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP');
              },
              "width": "50%",
              dummy: ''
            }, {
              name: 'warehouse',
              title: 'Warehouse',
              type: '_id_197',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'M_Warehouse_ID',
              inpColumnName: 'inpmWarehouseId',
              referencedKeyColumnName: 'M_Warehouse_ID',
              targetEntity: 'Warehouse',
              required: true,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (currentValues.directShipment === false);
              },
              width: '*',
              dummy: ''
            }, {
              name: 'reservedQuantity',
              title: 'Reserved Quantity',
              type: '_id_29',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'QtyReserved',
              inpColumnName: 'inpqtyreserved',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.OrderType === 'OB' || context.OrderType === 'SO' || context.Processed === 'Y');
              },
              "width": "50%",
              dummy: ''
            }, {
              name: 'shippingCompany',
              title: 'Shipping Company',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'M_Shipper_ID',
              inpColumnName: 'inpmShipperId',
              referencedKeyColumnName: 'M_Shipper_ID',
              targetEntity: 'ShippingShippingCompany',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.DeliveryViaRule === 'S');
              },
              width: '*',
              dummy: ''
            }, {
              name: 'businessPartner',
              title: 'Business Partner',
              type: '_id_800057',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'C_BPartner_ID',
              inpColumnName: 'inpcBpartnerId',
              referencedKeyColumnName: 'C_BPartner_ID',
              targetEntity: 'BusinessPartner',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && ((context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP' || context.Processed === 'Y') && currentValues.orderDiscount === '');
              },
              selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
              popupTextMatchStyle: 'startsWith',
              textMatchStyle: 'startsWith',
              defaultPopupFilterField: 'name',
              displayField: 'name',
              valueField: 'bpid',
              pickListFields: [{
                title: ' ',
                name: 'name',
                disableFilter: true,
                canSort: false,
                type: 'text'
              }, {
                title: 'Location',
                name: 'locationname',
                disableFilter: true,
                canSort: false,
                type: '_id_10'
              }, {
                title: 'Contact',
                name: 'contactname',
                disableFilter: true,
                canSort: false,
                type: '_id_10'
              }],
              showSelectorGrid: true,
              selectorGridFields: [{
                title: 'Name',
                name: 'name',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Value',
                name: 'value',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Credit Line available',
                name: 'creditAvailable',
                disableFilter: false,
                canSort: true,
                type: '_id_12',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Customer Balance',
                name: 'creditUsed',
                disableFilter: false,
                canSort: true,
                type: '_id_12',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Location',
                name: 'locationname',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Contact',
                name: 'contactname',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Customer',
                name: 'customer',
                disableFilter: false,
                canSort: true,
                type: '_id_20',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBYesNoItem'
              }, {
                title: 'Vendor',
                name: 'vendor',
                disableFilter: false,
                canSort: true,
                type: '_id_20',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBYesNoItem'
              }, {
                title: 'Income',
                name: 'income',
                disableFilter: false,
                canSort: true,
                type: '_id_12',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }],
              outFields: {
                'id': {
                  'fieldName': '',
                  'suffix': ''
                },
                '_identifier': {
                  'fieldName': '',
                  'suffix': ''
                },
                'locationid': {
                  'fieldName': 'locationid',
                  'suffix': '_LOC'
                },
                'contactid': {
                  'fieldName': 'contactid',
                  'suffix': '_CON'
                }
              },
              extraSearchFields: ['value'],
              optionDataSource: OB.Datasource.create({
                createClassName: '',
                titleField: OB.Constants.IDENTIFIER,
                dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
                recordXPath: '/response/data',
                dataFormat: 'json',
                operationBindings: [{
                  operationType: 'fetch',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'POST'
                  }
                }, {
                  operationType: 'add',
                  dataProtocol: 'postMessage'
                }, {
                  operationType: 'remove',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'DELETE'
                  }
                }, {
                  operationType: 'update',
                  dataProtocol: 'postMessage',
                  requestProperties: {
                    httpMethod: 'PUT'
                  }
                }],
                requestProperties: {
                  params: {
                    targetProperty: 'businessPartner',
                    adTabId: '187',
                    IsSelectorItem: 'true',
                    columnName: 'C_BPartner_ID',
                    _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
                  }
                },
                fields: []
              }),
              whereClause: '',
              outHiddenInputPrefix: 'inpcBpartnerId',
              width: '*',
              dummy: ''
            }, {
              name: 'directShipment',
              title: 'Direct shipment',
              type: '_id_20',
              disabled: false,
              readonly: false,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'DirectShip',
              inpColumnName: 'inpdirectship',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              redrawOnChange: true,
              changed: function (form, item, value) {
                if (this.pickValue && !this._pickedValue) {
                  return;
                }
                this.Super('changed', arguments);
                form.onFieldChanged(form, item, value);
                form.view.toolBar.refreshCustomButtonsView(form.view);
              },
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: 'freightAmount',
              title: 'Freight Amount',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'FreightAmt',
              inpColumnName: 'inpfreightamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.FreightCostRule === 'L');
              },
              "width": "50%",
              dummy: ''
            }, {
              name: 'partnerAddress',
              title: 'Partner Address',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'C_BPartner_Location_ID',
              inpColumnName: 'inpcBpartnerLocationId',
              referencedKeyColumnName: 'C_BPartner_Location_ID',
              targetEntity: 'BusinessPartnerLocation',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && ((context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP' || context.Processed === 'Y') && currentValues.orderDiscount === '');
              },
              width: '*',
              dummy: ''
            }, {
              name: 'cancelPriceAdjustment',
              title: 'Cancel Price Adjustment',
              type: '_id_20',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'CANCELPRICEAD',
              inpColumnName: 'inpcancelpricead',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: 'orderUOM',
              title: 'Order UOM',
              type: '_id_800000',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'M_Product_Uom_Id',
              inpColumnName: 'inpmProductUomId',
              referencedKeyColumnName: 'M_Product_Uom_Id',
              targetEntity: 'ProductUOM',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.HASSECONDUOM === 1);
              },
              width: '*',
              dummy: ''
            }, {
              name: 'orderQuantity',
              title: 'Order Quantity',
              type: '_id_29',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'QuantityOrder',
              inpColumnName: 'inpquantityorder',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              showIf: function (item, value, form, values) {
                var context = form.view.getContextInfo(false, true, true),
                    currentValues = values || form.view.getCurrentValues();
                OB.Utilities.fixNull250(currentValues);
                return !this.hiddenInForm && context && (context.HASSECONDUOM === 1);
              },
              "width": "50%",
              dummy: ''
            }, {
              name: 'standardPrice',
              title: 'Base Net Unit Price',
              type: '_id_800008',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'PriceStd',
              inpColumnName: 'inppricestd',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'editLineAmount',
              title: 'Edit Line Net Amount',
              type: '_id_20',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Iseditlinenetamt',
              inpColumnName: 'inpiseditlinenetamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              redrawOnChange: true,
              changed: function (form, item, value) {
                if (this.pickValue && !this._pickedValue) {
                  return;
                }
                this.Super('changed', arguments);
                form.onFieldChanged(form, item, value);
                form.view.toolBar.refreshCustomButtonsView(form.view);
              },
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: '1000100001',
              title: 'Audit',
              type: 'OBAuditSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'Audit',
              itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
              dummy: ''
            }, {
              name: 'creationDate',
              title: 'Creation Date',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'createdBy',
              title: 'Created By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: 'updated',
              title: 'Updated',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'updatedBy',
              title: 'Updated By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: '_notes_',
              title: 'dummy',
              type: 'OBNoteSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_notes_Canvas'],
              dummy: ''
            }, {
              name: '_notes_Canvas',
              title: 'dummy',
              type: 'OBNoteCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_linkedItems_',
              title: 'dummy',
              type: 'OBLinkedItemSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_linkedItems_Canvas'],
              dummy: ''
            }, {
              name: '_linkedItems_Canvas',
              title: 'dummy',
              type: 'OBLinkedItemCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_attachments_',
              title: 'dummy',
              type: 'OBAttachmentsSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_attachments_Canvas'],
              dummy: ''
            }, {
              name: '_attachments_Canvas',
              title: '',
              type: 'OBAttachmentCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: 'invoicedQuantity',
              title: 'Invoiced Quantity',
              type: '_id_29',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'QtyInvoiced',
              inpColumnName: 'inpqtyinvoiced',
              referencedKeyColumnName: '',
              targetEntity: '',
              visible: false,
              displayed: false,
              alwaysTakeSpace: false,
              required: true,
              "width": "",
              dummy: ''
            }, {
              name: 'deliveredQuantity',
              title: 'Delivered Quantity',
              type: '_id_29',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'QtyDelivered',
              inpColumnName: 'inpqtydelivered',
              referencedKeyColumnName: '',
              targetEntity: '',
              visible: false,
              displayed: false,
              alwaysTakeSpace: false,
              required: true,
              "width": "",
              dummy: ''
            }],
            statusBarFields: ['invoicedQuantity', 'deliveredQuantity'],
            obFormProperties: {
              onFieldChanged: function (form, item, value) {
                var f = form || this,
                    context = this.view.getContextInfo(false, true),
                    currentValues = f.view.getCurrentValues(),
                    otherItem;
                otherItem = f.getItem('lineNo');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('product');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('orderedQuantity');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('attributeSetValue');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('unitPrice');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('lineNetAmount');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (currentValues.editLineAmount === false) {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('tax');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('listPrice');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('taxableAmount');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('warehouse');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.OrderType !== 'SO') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('directShipment');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('orderUOM');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('orderQuantity');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('editLineAmount');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
              }
            }
          });
          this.viewGrid = isc.OBViewGrid.create({
            uiPattern: 'STD',
            fields: [{
              autoExpand: false,
              type: '_id_11',
              editorProperties: {
                "width": "50%",
                columnName: 'Line',
                inpColumnName: 'inpline',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'lineNo',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Line No.',
              prompt: 'Line No.',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Line',
              inpColumnName: 'inpline',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_800060',
              editorProperties: {
                selectorDefinitionId: '2E64F551C7C4470C80C29DBA24B34A5F',
                popupTextMatchStyle: 'startsWith',
                textMatchStyle: 'startsWith',
                defaultPopupFilterField: '_identifier',
                displayField: '_identifier',
                valueField: 'product.id',
                pickListFields: [{
                  title: ' ',
                  name: '_identifier',
                  disableFilter: true,
                  canSort: false,
                  type: 'text'
                }],
                showSelectorGrid: true,
                selectorGridFields: [{
                  title: 'Search Key',
                  name: 'product.searchKey',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Name',
                  name: 'product.name',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Available',
                  name: 'available',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_29',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Warehouse',
                  name: 'warehouse',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_19',
                  displayField: 'warehouse._identifier',
                  filterOperator: 'equals',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  filterEditorType: 'OBSelectorFilterSelectItem',
                  filterEditorProperties: {
                    entity: 'Warehouse',
                    displayField: '_identifier'
                  }
                }, {
                  title: 'Net Unit Price',
                  name: 'standardPrice',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_800008',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Net List Price',
                  name: 'netListPrice',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_800008',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Price List Version',
                  name: 'productPrice.priceListVersion',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_19',
                  displayField: 'productPrice.priceListVersion._identifier',
                  filterOperator: 'equals',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  filterEditorType: 'OBSelectorFilterSelectItem',
                  filterEditorProperties: {
                    entity: 'PricingPriceListVersion',
                    displayField: '_identifier'
                  }
                }, {
                  title: 'Warehouse Qty.',
                  name: 'qtyOnHand',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_29',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Ordered Qty.',
                  name: 'qtyOrdered',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_29',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Net Price Limit',
                  name: 'priceLimit',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_800008',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }],
                outFields: {
                  'id': {
                    'fieldName': '',
                    'suffix': ''
                  },
                  '_identifier': {
                    'fieldName': '',
                    'suffix': ''
                  },
                  'productPrice.priceListVersion.priceList.currency.id': {
                    'fieldName': 'productPrice.priceListVersion.priceList.currency.id',
                    'suffix': '_CURR'
                  },
                  'product.uOM.id': {
                    'fieldName': 'product.uOM.id',
                    'suffix': '_UOM'
                  },
                  'standardPrice': {
                    'fieldName': 'standardPrice',
                    'suffix': '_PSTD'
                  },
                  'netListPrice': {
                    'fieldName': 'netListPrice',
                    'suffix': '_PLIST'
                  },
                  'priceLimit': {
                    'fieldName': 'priceLimit',
                    'suffix': '_PLIM'
                  }
                },
                extraSearchFields: ['product.name', 'product._identifier', 'product.searchKey'],
                optionDataSource: OB.Datasource.create({
                  createClassName: '',
                  titleField: OB.Constants.IDENTIFIER,
                  dataURL: '/openbravo/org.openbravo.service.datasource/ProductByPriceAndWarehouse',
                  recordXPath: '/response/data',
                  dataFormat: 'json',
                  operationBindings: [{
                    operationType: 'fetch',
                    dataProtocol: 'postParams',
                    requestProperties: {
                      httpMethod: 'POST'
                    }
                  }, {
                    operationType: 'add',
                    dataProtocol: 'postMessage'
                  }, {
                    operationType: 'remove',
                    dataProtocol: 'postParams',
                    requestProperties: {
                      httpMethod: 'DELETE'
                    }
                  }, {
                    operationType: 'update',
                    dataProtocol: 'postMessage',
                    requestProperties: {
                      httpMethod: 'PUT'
                    }
                  }],
                  requestProperties: {
                    params: {
                      targetProperty: 'product',
                      adTabId: '187',
                      IsSelectorItem: 'true',
                      columnName: 'M_Product_ID',
                      _extraProperties: 'product.id,productPrice.priceListVersion._identifier,available,warehouse._identifier,productPrice.priceListVersion.priceList.currency.id,product.name,qtyOnHand,product._identifier,qtyOrdered,netListPrice,product.uOM.id,priceLimit,standardPrice,product.searchKey'
                    }
                  },
                  fields: [{
                    name: 'id',
                    type: '_id_13',
                    additional: false,
                    hidden: true,
                    primaryKey: true,
                    canSave: false,
                    title: 'id'
                  }, {
                    name: 'client',
                    type: '_id_19',
                    additional: false,
                    canSave: false,
                    title: 'client',
                    hidden: true
                  }, {
                    name: 'client._identifier',
                    type: 'text',
                    hidden: true,
                    canSave: false,
                    title: 'client'
                  }, {
                    name: 'organization',
                    type: '_id_19',
                    additional: false,
                    canSave: false,
                    title: 'organization',
                    hidden: true
                  }, {
                    name: 'organization._identifier',
                    type: 'text',
                    hidden: true,
                    canSave: false,
                    title: 'organization'
                  }, {
                    name: 'active',
                    type: '_id_20',
                    additional: false,
                    title: 'active'
                  }, {
                    name: 'updated',
                    type: '_id_16',
                    additional: false,
                    canSave: false,
                    title: 'updated'
                  }, {
                    name: 'updatedBy',
                    type: '_id_30',
                    additional: false,
                    canSave: false,
                    title: 'updatedBy',
                    hidden: true
                  }, {
                    name: 'updatedBy._identifier',
                    type: 'text',
                    hidden: true,
                    canSave: false,
                    title: 'updatedBy'
                  }, {
                    name: 'creationDate',
                    type: '_id_16',
                    additional: false,
                    canSave: false,
                    title: 'creationDate'
                  }, {
                    name: 'createdBy',
                    type: '_id_30',
                    additional: false,
                    canSave: false,
                    title: 'createdBy',
                    hidden: true
                  }, {
                    name: 'createdBy._identifier',
                    type: 'text',
                    hidden: true,
                    canSave: false,
                    title: 'createdBy'
                  }, {
                    name: 'product',
                    type: '_id_800060',
                    additional: false,
                    required: true,
                    title: 'product',
                    hidden: true
                  }, {
                    name: 'product._identifier',
                    type: 'text',
                    hidden: true,
                    title: 'product'
                  }, {
                    name: 'warehouse',
                    type: '_id_19',
                    additional: false,
                    required: true,
                    title: 'warehouse',
                    hidden: true
                  }, {
                    name: 'warehouse._identifier',
                    type: 'text',
                    hidden: true,
                    title: 'warehouse'
                  }, {
                    name: 'productPrice',
                    type: '_id_19',
                    additional: false,
                    required: true,
                    title: 'productPrice',
                    hidden: true
                  }, {
                    name: 'productPrice._identifier',
                    type: 'text',
                    hidden: true,
                    title: 'productPrice'
                  }, {
                    name: 'available',
                    type: '_id_29',
                    additional: false,
                    title: 'available'
                  }, {
                    name: 'qtyOnHand',
                    type: '_id_29',
                    additional: false,
                    title: 'qtyOnHand'
                  }, {
                    name: 'qtyReserved',
                    type: '_id_29',
                    additional: false,
                    title: 'qtyReserved'
                  }, {
                    name: 'qtyOrdered',
                    type: '_id_29',
                    additional: false,
                    title: 'qtyOrdered'
                  }, {
                    name: 'netListPrice',
                    type: '_id_800008',
                    additional: false,
                    title: 'netListPrice'
                  }, {
                    name: 'standardPrice',
                    type: '_id_800008',
                    additional: false,
                    title: 'standardPrice'
                  }, {
                    name: 'priceLimit',
                    type: '_id_800008',
                    additional: false,
                    title: 'priceLimit'
                  }, {
                    name: 'product.id',
                    type: '_id_13',
                    additional: true,
                    hidden: true,
                    primaryKey: true,
                    canSave: false,
                    title: 'product.id'
                  }, {
                    name: 'productPrice.priceListVersion._identifier',
                    type: '_id_10',
                    additional: true,
                    required: true,
                    length: 60,
                    title: 'productPrice.priceListVersion._identifier'
                  }, {
                    name: 'available',
                    type: '_id_29',
                    additional: true,
                    title: 'available'
                  }, {
                    name: 'warehouse._identifier',
                    type: '_id_10',
                    additional: true,
                    required: true,
                    length: 60,
                    title: 'warehouse._identifier'
                  }, {
                    name: 'productPrice.priceListVersion.priceList.currency.id',
                    type: '_id_13',
                    additional: true,
                    hidden: true,
                    primaryKey: true,
                    canSave: false,
                    title: 'productPrice.priceListVersion.priceList.currency.id'
                  }, {
                    name: 'product.name',
                    type: '_id_10',
                    additional: true,
                    required: true,
                    length: 60,
                    title: 'product.name'
                  }, {
                    name: 'qtyOnHand',
                    type: '_id_29',
                    additional: true,
                    title: 'qtyOnHand'
                  }, {
                    name: 'product._identifier',
                    type: '_id_10',
                    additional: true,
                    required: true,
                    length: 60,
                    title: 'product._identifier'
                  }, {
                    name: 'qtyOrdered',
                    type: '_id_29',
                    additional: true,
                    title: 'qtyOrdered'
                  }, {
                    name: 'netListPrice',
                    type: '_id_800008',
                    additional: true,
                    title: 'netListPrice'
                  }, {
                    name: 'product.uOM.id',
                    type: '_id_13',
                    additional: true,
                    hidden: true,
                    primaryKey: true,
                    canSave: false,
                    title: 'product.uOM.id'
                  }, {
                    name: 'priceLimit',
                    type: '_id_800008',
                    additional: true,
                    title: 'priceLimit'
                  }, {
                    name: 'standardPrice',
                    type: '_id_800008',
                    additional: true,
                    title: 'standardPrice'
                  }, {
                    name: 'product.searchKey',
                    type: '_id_10',
                    additional: true,
                    required: true,
                    length: 40,
                    title: 'product.searchKey'
                  }]
                }),
                whereClause: '',
                outHiddenInputPrefix: 'inpmProductId',
                width: '*',
                columnName: 'M_Product_ID',
                inpColumnName: 'inpmProductId',
                referencedKeyColumnName: 'M_Product_ID',
                targetEntity: 'Product',
                disabled: false,
                readonly: false,
                updatable: true,
                firstFocusedField: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'product._identifier',
              valueField: 'product',
              foreignKeyField: true,
              name: 'product',
              canExport: true,
              canHide: true,
              editorType: 'OBSelectorItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'product')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Product',
              prompt: 'Product',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'M_Product_ID',
              inpColumnName: 'inpmProductId',
              referencedKeyColumnName: 'M_Product_ID',
              targetEntity: 'Product'
            }, {
              autoExpand: false,
              type: '_id_29',
              editorProperties: {
                "width": "50%",
                columnName: 'QtyOrdered',
                inpColumnName: 'inpqtyordered',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'orderedQuantity',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Ordered Quantity',
              prompt: 'Ordered Quantity',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'QtyOrdered',
              inpColumnName: 'inpqtyordered',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_35',
              editorProperties: {
                width: '*',
                columnName: 'M_AttributeSetInstance_ID',
                inpColumnName: 'inpmAttributesetinstanceId',
                referencedKeyColumnName: 'M_AttributeSetInstance_ID',
                targetEntity: 'AttributeSetInstance',
                disabled: false,
                readonly: false,
                updatable: true,
                redrawOnChange: true,
                changed: function (form, item, value) {
                  if (this.pickValue && !this._pickedValue) {
                    return;
                  }
                  this.Super('changed', arguments);
                  form.onFieldChanged(form, item, value);
                  form.view.toolBar.refreshCustomButtonsView(form.view);
                },
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && ((context.ATTRIBUTESET !== '' && context.ATTRSETVALUETYPE !== 'F') || (currentValues.attributeSetValue !== '' && currentValues.attributeSetValue !== '0'));
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(14),
              displayField: 'attributeSetValue._identifier',
              valueField: 'attributeSetValue',
              foreignKeyField: true,
              name: 'attributeSetValue',
              canExport: true,
              canHide: true,
              editorType: 'OBPAttributeSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'attributeSetValue')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Attribute Set Value',
              prompt: 'Attribute Set Value',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'M_AttributeSetInstance_ID',
              inpColumnName: 'inpmAttributesetinstanceId',
              referencedKeyColumnName: 'M_AttributeSetInstance_ID',
              targetEntity: 'AttributeSetInstance'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_UOM_ID',
                inpColumnName: 'inpcUomId',
                referencedKeyColumnName: 'C_UOM_ID',
                targetEntity: 'UOM',
                disabled: true,
                readonly: true,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'uOM._identifier',
              valueField: 'uOM',
              foreignKeyField: true,
              name: 'uOM',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'uOM')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'UOM',
              prompt: 'UOM',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_UOM_ID',
              inpColumnName: 'inpcUomId',
              referencedKeyColumnName: 'C_UOM_ID',
              targetEntity: 'UOM'
            }, {
              autoExpand: false,
              type: '_id_800008',
              editorProperties: {
                "width": "50%",
                columnName: 'PriceActual',
                inpColumnName: 'inppriceactual',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'unitPrice',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Net Unit Price',
              prompt: 'Net Unit Price',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'PriceActual',
              inpColumnName: 'inppriceactual',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'LineNetAmt',
                inpColumnName: 'inplinenetamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'lineNetAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Line Net Amount',
              prompt: 'Line Net Amount',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'LineNetAmt',
              inpColumnName: 'inplinenetamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_158',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Tax_ID',
                inpColumnName: 'inpcTaxId',
                referencedKeyColumnName: 'C_Tax_ID',
                targetEntity: 'FinancialMgmtTaxRate',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'tax._identifier',
              valueField: 'tax',
              foreignKeyField: true,
              name: 'tax',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'tax')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Tax',
              prompt: 'Tax',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Tax_ID',
              inpColumnName: 'inpcTaxId',
              referencedKeyColumnName: 'C_Tax_ID',
              targetEntity: 'FinancialMgmtTaxRate'
            }, {
              autoExpand: false,
              type: '_id_800008',
              editorProperties: {
                "width": "50%",
                columnName: 'PriceList',
                inpColumnName: 'inppricelist',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'listPrice',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Net List Price',
              prompt: 'Net List Price',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'PriceList',
              inpColumnName: 'inppricelist',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_22',
              editorProperties: {
                "width": "50%",
                columnName: 'Discount',
                inpColumnName: 'inpdiscount',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'discount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Discount',
              prompt: 'Discount',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Discount',
              inpColumnName: 'inpdiscount',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_14',
              editorProperties: {
                width: '*',
                columnName: 'Description',
                inpColumnName: 'inpdescription',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'description')])",
              width: isc.OBGrid.getDefaultColumnWidth(60),
              name: 'description',
              canExport: true,
              canHide: true,
              editorType: 'OBPopUpTextAreaItem',
              canSort: false,
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextItem',
              title: 'Description',
              prompt: 'Description',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Description',
              inpColumnName: 'inpdescription',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'Taxbaseamt',
                inpColumnName: 'inptaxbaseamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.UsesAlternate === 'Y');
                }
              },
              name: 'taxableAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Alternate Taxable Amount',
              prompt: 'Alternate Taxable Amount',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Taxbaseamt',
              inpColumnName: 'inptaxbaseamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_29',
              editorProperties: {
                "width": "",
                columnName: 'QtyInvoiced',
                inpColumnName: 'inpqtyinvoiced',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false
              },
              name: 'invoicedQuantity',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Invoiced Quantity',
              prompt: 'Invoiced Quantity',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'QtyInvoiced',
              inpColumnName: 'inpqtyinvoiced',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_29',
              editorProperties: {
                "width": "",
                columnName: 'QtyDelivered',
                inpColumnName: 'inpqtydelivered',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false
              },
              name: 'deliveredQuantity',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Delivered Quantity',
              prompt: 'Delivered Quantity',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'QtyDelivered',
              inpColumnName: 'inpqtydelivered',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'DateOrdered',
                inpColumnName: 'inpdateordered',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP');
                }
              },
              name: 'orderDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Order Date',
              prompt: 'Order Date',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'DateOrdered',
              inpColumnName: 'inpdateordered',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'DatePromised',
                inpColumnName: 'inpdatepromised',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP');
                }
              },
              name: 'scheduledDeliveryDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Scheduled Delivery Date',
              prompt: 'Scheduled Delivery Date',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'DatePromised',
              inpColumnName: 'inpdatepromised',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_197',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'M_Warehouse_ID',
                inpColumnName: 'inpmWarehouseId',
                referencedKeyColumnName: 'M_Warehouse_ID',
                targetEntity: 'Warehouse',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (currentValues.directShipment === false);
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'warehouse._identifier',
              valueField: 'warehouse',
              foreignKeyField: true,
              name: 'warehouse',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'warehouse')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Warehouse',
              prompt: 'Warehouse',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'M_Warehouse_ID',
              inpColumnName: 'inpmWarehouseId',
              referencedKeyColumnName: 'M_Warehouse_ID',
              targetEntity: 'Warehouse'
            }, {
              autoExpand: false,
              type: '_id_29',
              editorProperties: {
                "width": "50%",
                columnName: 'QtyReserved',
                inpColumnName: 'inpqtyreserved',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.OrderType === 'OB' || context.OrderType === 'SO' || context.Processed === 'Y');
                }
              },
              name: 'reservedQuantity',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Reserved Quantity',
              prompt: 'Reserved Quantity',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'QtyReserved',
              inpColumnName: 'inpqtyreserved',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'M_Shipper_ID',
                inpColumnName: 'inpmShipperId',
                referencedKeyColumnName: 'M_Shipper_ID',
                targetEntity: 'ShippingShippingCompany',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.DeliveryViaRule === 'S');
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'shippingCompany._identifier',
              valueField: 'shippingCompany',
              foreignKeyField: true,
              name: 'shippingCompany',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'shippingCompany')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Shipping Company',
              prompt: 'Shipping Company',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'M_Shipper_ID',
              inpColumnName: 'inpmShipperId',
              referencedKeyColumnName: 'M_Shipper_ID',
              targetEntity: 'ShippingShippingCompany'
            }, {
              autoExpand: true,
              type: '_id_800057',
              editorProperties: {
                selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
                popupTextMatchStyle: 'startsWith',
                textMatchStyle: 'startsWith',
                defaultPopupFilterField: 'name',
                displayField: 'name',
                valueField: 'bpid',
                pickListFields: [{
                  title: ' ',
                  name: 'name',
                  disableFilter: true,
                  canSort: false,
                  type: 'text'
                }, {
                  title: 'Location',
                  name: 'locationname',
                  disableFilter: true,
                  canSort: false,
                  type: '_id_10'
                }, {
                  title: 'Contact',
                  name: 'contactname',
                  disableFilter: true,
                  canSort: false,
                  type: '_id_10'
                }],
                showSelectorGrid: true,
                selectorGridFields: [{
                  title: 'Name',
                  name: 'name',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Value',
                  name: 'value',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Credit Line available',
                  name: 'creditAvailable',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_12',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Customer Balance',
                  name: 'creditUsed',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_12',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Location',
                  name: 'locationname',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Contact',
                  name: 'contactname',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Customer',
                  name: 'customer',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_20',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem'
                }, {
                  title: 'Vendor',
                  name: 'vendor',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_20',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem'
                }, {
                  title: 'Income',
                  name: 'income',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_12',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }],
                outFields: {
                  'id': {
                    'fieldName': '',
                    'suffix': ''
                  },
                  '_identifier': {
                    'fieldName': '',
                    'suffix': ''
                  },
                  'locationid': {
                    'fieldName': 'locationid',
                    'suffix': '_LOC'
                  },
                  'contactid': {
                    'fieldName': 'contactid',
                    'suffix': '_CON'
                  }
                },
                extraSearchFields: ['value'],
                optionDataSource: OB.Datasource.create({
                  createClassName: '',
                  titleField: OB.Constants.IDENTIFIER,
                  dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
                  recordXPath: '/response/data',
                  dataFormat: 'json',
                  operationBindings: [{
                    operationType: 'fetch',
                    dataProtocol: 'postParams',
                    requestProperties: {
                      httpMethod: 'POST'
                    }
                  }, {
                    operationType: 'add',
                    dataProtocol: 'postMessage'
                  }, {
                    operationType: 'remove',
                    dataProtocol: 'postParams',
                    requestProperties: {
                      httpMethod: 'DELETE'
                    }
                  }, {
                    operationType: 'update',
                    dataProtocol: 'postMessage',
                    requestProperties: {
                      httpMethod: 'PUT'
                    }
                  }],
                  requestProperties: {
                    params: {
                      targetProperty: 'businessPartner',
                      adTabId: '187',
                      IsSelectorItem: 'true',
                      columnName: 'C_BPartner_ID',
                      _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
                    }
                  },
                  fields: []
                }),
                whereClause: '',
                outHiddenInputPrefix: 'inpcBpartnerId',
                width: '*',
                columnName: 'C_BPartner_ID',
                inpColumnName: 'inpcBpartnerId',
                referencedKeyColumnName: 'C_BPartner_ID',
                targetEntity: 'BusinessPartner',
                disabled: true,
                readonly: true,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && ((context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP' || context.Processed === 'Y') && currentValues.orderDiscount === '');
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'businessPartner._identifier',
              valueField: 'businessPartner',
              foreignKeyField: true,
              name: 'businessPartner',
              canExport: true,
              canHide: true,
              editorType: 'OBSelectorItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'businessPartner')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Business Partner',
              prompt: 'Business Partner',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_BPartner_ID',
              inpColumnName: 'inpcBpartnerId',
              referencedKeyColumnName: 'C_BPartner_ID',
              targetEntity: 'BusinessPartner'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'DirectShip',
                inpColumnName: 'inpdirectship',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: false,
                redrawOnChange: true,
                changed: function (form, item, value) {
                  if (this.pickValue && !this._pickedValue) {
                    return;
                  }
                  this.Super('changed', arguments);
                  form.onFieldChanged(form, item, value);
                  form.view.toolBar.refreshCustomButtonsView(form.view);
                }
              },
              name: 'directShipment',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Direct shipment',
              prompt: 'Direct shipment',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'DirectShip',
              inpColumnName: 'inpdirectship',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'FreightAmt',
                inpColumnName: 'inpfreightamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.FreightCostRule === 'L');
                }
              },
              name: 'freightAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Freight Amount',
              prompt: 'Freight Amount',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'FreightAmt',
              inpColumnName: 'inpfreightamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_BPartner_Location_ID',
                inpColumnName: 'inpcBpartnerLocationId',
                referencedKeyColumnName: 'C_BPartner_Location_ID',
                targetEntity: 'BusinessPartnerLocation',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && ((context.OrderType === 'OB' || context.OrderType === 'SO' || context.OrderType === 'WP' || context.Processed === 'Y') && currentValues.orderDiscount === '');
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'partnerAddress._identifier',
              valueField: 'partnerAddress',
              foreignKeyField: true,
              name: 'partnerAddress',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'partnerAddress')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Partner Address',
              prompt: 'Partner Address',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_BPartner_Location_ID',
              inpColumnName: 'inpcBpartnerLocationId',
              referencedKeyColumnName: 'C_BPartner_Location_ID',
              targetEntity: 'BusinessPartnerLocation'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'CANCELPRICEAD',
                inpColumnName: 'inpcancelpricead',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'cancelPriceAdjustment',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Cancel Price Adjustment',
              prompt: 'Cancel Price Adjustment',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'CANCELPRICEAD',
              inpColumnName: 'inpcancelpricead',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_800000',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'M_Product_Uom_Id',
                inpColumnName: 'inpmProductUomId',
                referencedKeyColumnName: 'M_Product_Uom_Id',
                targetEntity: 'ProductUOM',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.HASSECONDUOM === 1);
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'orderUOM._identifier',
              valueField: 'orderUOM',
              foreignKeyField: true,
              name: 'orderUOM',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'orderUOM')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Order UOM',
              prompt: 'Order UOM',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'M_Product_Uom_Id',
              inpColumnName: 'inpmProductUomId',
              referencedKeyColumnName: 'M_Product_Uom_Id',
              targetEntity: 'ProductUOM'
            }, {
              autoExpand: false,
              type: '_id_29',
              editorProperties: {
                "width": "50%",
                columnName: 'QuantityOrder',
                inpColumnName: 'inpquantityorder',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                showIf: function (item, value, form, currentValues) {
                  currentValues = currentValues || form.view.getCurrentValues();
                  var context = form.view.getContextInfo(false, true);
                  return context && (context.HASSECONDUOM === 1);
                }
              },
              name: 'orderQuantity',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Order Quantity',
              prompt: 'Order Quantity',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'QuantityOrder',
              inpColumnName: 'inpquantityorder',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_800008',
              editorProperties: {
                "width": "50%",
                columnName: 'PriceStd',
                inpColumnName: 'inppricestd',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: true
              },
              name: 'standardPrice',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Base Net Unit Price',
              prompt: 'Base Net Unit Price',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'PriceStd',
              inpColumnName: 'inppricestd',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'Iseditlinenetamt',
                inpColumnName: 'inpiseditlinenetamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                redrawOnChange: true,
                changed: function (form, item, value) {
                  if (this.pickValue && !this._pickedValue) {
                    return;
                  }
                  this.Super('changed', arguments);
                  form.onFieldChanged(form, item, value);
                  form.view.toolBar.refreshCustomButtonsView(form.view);
                }
              },
              name: 'editLineAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Edit Line Net Amount',
              prompt: 'Edit Line Net Amount',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Iseditlinenetamt',
              inpColumnName: 'inpiseditlinenetamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Client_ID',
                inpColumnName: 'inpadClientId',
                referencedKeyColumnName: 'AD_Client_ID',
                targetEntity: 'ADClient',
                disabled: false,
                readonly: false,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'client._identifier',
              valueField: 'client',
              foreignKeyField: true,
              name: 'client',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Client',
              prompt: 'Client',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Client_ID',
              inpColumnName: 'inpadClientId',
              referencedKeyColumnName: 'AD_Client_ID',
              targetEntity: 'ADClient'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsActive',
                inpColumnName: 'inpisactive',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'active',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Active',
              prompt: 'Active',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsActive',
              inpColumnName: 'inpisactive',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'DateDelivered',
                inpColumnName: 'inpdatedelivered',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false
              },
              name: 'dateDelivered',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Date Delivered',
              prompt: 'Date Delivered',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'DateDelivered',
              inpColumnName: 'inpdatedelivered',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'DateInvoiced',
                inpColumnName: 'inpdateinvoiced',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false
              },
              name: 'invoiceDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Invoice Date',
              prompt: 'Invoice Date',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'DateInvoiced',
              inpColumnName: 'inpdateinvoiced',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Currency_ID',
                inpColumnName: 'inpcCurrencyId',
                referencedKeyColumnName: 'C_Currency_ID',
                targetEntity: 'Currency',
                disabled: true,
                readonly: true,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'currency._identifier',
              valueField: 'currency',
              foreignKeyField: true,
              name: 'currency',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'currency')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Currency',
              prompt: 'Currency',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_Currency_ID',
              inpColumnName: 'inpcCurrencyId',
              referencedKeyColumnName: 'C_Currency_ID',
              targetEntity: 'Currency'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Org_ID',
                inpColumnName: 'inpadOrgId',
                referencedKeyColumnName: 'AD_Org_ID',
                targetEntity: 'Organization',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'organization._identifier',
              valueField: 'organization',
              foreignKeyField: true,
              name: 'organization',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Organization',
              prompt: 'Organization',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Org_ID',
              inpColumnName: 'inpadOrgId',
              referencedKeyColumnName: 'AD_Org_ID',
              targetEntity: 'Organization'
            }, {
              autoExpand: false,
              type: '_id_800008',
              editorProperties: {
                "width": "",
                columnName: 'PriceLimit',
                inpColumnName: 'inppricelimit',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: true
              },
              name: 'priceLimit',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Net Price Limit',
              prompt: 'Net Price Limit',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'PriceLimit',
              inpColumnName: 'inppricelimit',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_33',
              editorProperties: {
                width: '',
                columnName: 'S_ResourceAssignment_ID',
                inpColumnName: 'inpsResourceassignmentId',
                referencedKeyColumnName: 'S_ResourceAssignment_ID',
                targetEntity: 'ResourceAssignment',
                disabled: false,
                readonly: false,
                updatable: true
              },
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'resourceAssignment')])",
              width: isc.OBGrid.getDefaultColumnWidth(14),
              name: 'resourceAssignment',
              canExport: true,
              canHide: true,
              editorType: 'OBTextItem',
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextFilterItem',
              title: 'Resource Assignment',
              prompt: 'Resource Assignment',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'S_ResourceAssignment_ID',
              inpColumnName: 'inpsResourceassignmentId',
              referencedKeyColumnName: 'S_ResourceAssignment_ID',
              targetEntity: 'ResourceAssignment'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Order_Discount_ID',
                inpColumnName: 'inpcOrderDiscountId',
                referencedKeyColumnName: 'C_Order_Discount_ID',
                targetEntity: 'OrderDiscount',
                disabled: false,
                readonly: false,
                updatable: true,
                redrawOnChange: true,
                changed: function (form, item, value) {
                  if (this.pickValue && !this._pickedValue) {
                    return;
                  }
                  this.Super('changed', arguments);
                  form.onFieldChanged(form, item, value);
                  form.view.toolBar.refreshCustomButtonsView(form.view);
                }
              },
              width: isc.OBGrid.getDefaultColumnWidth(32),
              displayField: 'orderDiscount._identifier',
              valueField: 'orderDiscount',
              foreignKeyField: true,
              name: 'orderDiscount',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'orderDiscount')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'C_Order_Discount_ID',
              prompt: 'C_Order_Discount_ID',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_Order_Discount_ID',
              inpColumnName: 'inpcOrderDiscountId',
              referencedKeyColumnName: 'C_Order_Discount_ID',
              targetEntity: 'OrderDiscount'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'M_Offer_ID',
                inpColumnName: 'inpmOfferId',
                referencedKeyColumnName: 'M_Offer_ID',
                targetEntity: 'PricingAdjustment',
                disabled: true,
                readonly: true,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'priceAdjustment._identifier',
              valueField: 'priceAdjustment',
              foreignKeyField: true,
              name: 'priceAdjustment',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'priceAdjustment')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Price Adjustment',
              prompt: 'Price Adjustment',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'M_Offer_ID',
              inpColumnName: 'inpmOfferId',
              referencedKeyColumnName: 'M_Offer_ID',
              targetEntity: 'PricingAdjustment'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsDescription',
                inpColumnName: 'inpisdescription',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'descriptionOnly',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Description Only',
              prompt: 'Description Only',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsDescription',
              inpColumnName: 'inpisdescription',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'creationDate',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'creationDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Creation Date',
              prompt: 'Creation Date',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'creationDate',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'createdBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'createdBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Created By',
              prompt: 'Created By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'createdBy',
              inpColumnName: '',
              targetEntity: 'User'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'updated',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updated',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated',
              prompt: 'Updated',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updated',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'updatedBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updatedBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated By',
              prompt: 'Updated By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updatedBy',
              inpColumnName: '',
              targetEntity: 'User'
            }],
            autoExpandFieldNames: ['description', 'product', 'uOM', 'tax', 'warehouse', 'shippingCompany', 'businessPartner', 'partnerAddress', 'orderUOM', 'client', 'currency', 'organization', 'priceAdjustment', 'orderDiscount', 'attributeSetValue', 'resourceAssignment'],
            whereClause: '',
            orderByClause: '',
            sortField: 'lineNo',
            filterClause: '',
            filterName: '',
            foreignKeyFieldNames: ['product', 'attributeSetValue', 'uOM', 'tax', 'warehouse', 'shippingCompany', 'businessPartner', 'partnerAddress', 'orderUOM', 'client', 'currency', 'organization', 'resourceAssignment', 'orderDiscount', 'priceAdjustment']
          });
          this.Super('initWidget', arguments);
        },
        createViewStructure: function () {
          this.addChildView(isc.OBStandardView.create({
            tabTitle: 'Price Adjustments',
            entity: 'OrderLineOffer',
            parentProperty: 'salesOrderLine',
            tabId: '800222',
            moduleId: '0',
            defaultEditMode: false,
            mapping250: '/SalesOrder/PriceAdjustments',
            isAcctTab: false,
            isTrlTab: false,
            standardProperties: {
              inpTabId: '800222',
              inpwindowId: '143',
              inpTableId: '800185',
              inpkeyColumnId: 'C_Orderline_Offer_ID',
              inpKeyName: 'inpcOrderlineOfferId'
            },
            propertyToColumns: [{
              property: 'active',
              inpColumn: 'inpisactive',
              dbColumn: 'IsActive',
              sessionProperty: false,
              type: '_id_20'
            }, {
              property: 'priceAdjustment',
              inpColumn: 'inpmOfferId',
              dbColumn: 'M_Offer_ID',
              sessionProperty: false,
              type: '_id_19'
            }, {
              property: 'lineNo',
              inpColumn: 'inpline',
              dbColumn: 'Line',
              sessionProperty: false,
              type: '_id_11'
            }, {
              property: 'priceAdjustmentAmt',
              inpColumn: 'inpamtoffer',
              dbColumn: 'Amtoffer',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'adjustedPrice',
              inpColumn: 'inppriceoffer',
              dbColumn: 'Priceoffer',
              sessionProperty: false,
              type: '_id_800008'
            }, {
              property: 'id',
              inpColumn: 'inpcOrderlineOfferId',
              dbColumn: 'C_Orderline_Offer_ID',
              sessionProperty: false,
              type: '_id_13'
            }, {
              property: 'client',
              inpColumn: 'inpadClientId',
              dbColumn: 'AD_Client_ID',
              sessionProperty: true,
              type: '_id_19'
            }, {
              property: 'organization',
              inpColumn: 'inpadOrgId',
              dbColumn: 'AD_Org_ID',
              sessionProperty: true,
              type: '_id_19'
            }, {
              property: 'salesOrderLine',
              inpColumn: 'inpcOrderlineId',
              dbColumn: 'C_OrderLine_ID',
              sessionProperty: false,
              type: '_id_30'
            }, {
              property: 'id',
              inpColumn: 'C_Orderline_Offer_ID',
              dbColumn: 'C_Orderline_Offer_ID',
              sessionProperty: true,
              type: '_id_13'
            }],
            actionToolbarButtons: [],
            showParentButtons: true,
            buttonsHaveSessionLogic: false,
            iconToolbarButtons: [],
            initWidget: function () {
              this.dataSource = OB.Datasource.create({
                createClassName: 'OBViewDataSource',
                titleField: OB.Constants.IDENTIFIER,
                dataURL: '/openbravo/org.openbravo.service.datasource/OrderLineOffer',
                recordXPath: '/response/data',
                dataFormat: 'json',
                operationBindings: [{
                  operationType: 'fetch',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'POST'
                  }
                }, {
                  operationType: 'add',
                  dataProtocol: 'postMessage'
                }, {
                  operationType: 'remove',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'DELETE'
                  }
                }, {
                  operationType: 'update',
                  dataProtocol: 'postMessage',
                  requestProperties: {
                    httpMethod: 'PUT'
                  }
                }],
                requestProperties: {
                  params: {
                    _className: 'OBViewDataSource'
                  }
                },
                fields: [{
                  name: 'id',
                  type: '_id_13',
                  additional: false,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'id'
                }, {
                  name: 'client',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'client',
                  hidden: true
                }, {
                  name: 'client._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'client'
                }, {
                  name: 'organization',
                  type: '_id_19',
                  additional: false,
                  required: true,
                  title: 'organization',
                  hidden: true
                }, {
                  name: 'organization._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'organization'
                }, {
                  name: 'active',
                  type: '_id_20',
                  additional: false,
                  title: 'active'
                }, {
                  name: 'creationDate',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'creationDate'
                }, {
                  name: 'createdBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'createdBy',
                  hidden: true
                }, {
                  name: 'createdBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'createdBy'
                }, {
                  name: 'updated',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'updated'
                }, {
                  name: 'updatedBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'updatedBy',
                  hidden: true
                }, {
                  name: 'updatedBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'updatedBy'
                }, {
                  name: 'salesOrderLine',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'salesOrderLine',
                  hidden: true
                }, {
                  name: 'salesOrderLine._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'salesOrderLine'
                }, {
                  name: 'lineNo',
                  type: '_id_11',
                  additional: false,
                  required: true,
                  title: 'lineNo'
                }, {
                  name: 'priceAdjustment',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'priceAdjustment',
                  hidden: true
                }, {
                  name: 'priceAdjustment._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'priceAdjustment'
                }, {
                  name: 'adjustedPrice',
                  type: '_id_800008',
                  additional: false,
                  canSave: false,
                  title: 'adjustedPrice'
                }, {
                  name: 'priceAdjustmentAmt',
                  type: '_id_12',
                  additional: false,
                  canSave: false,
                  title: 'priceAdjustmentAmt'
                }]
              });
              this.viewForm = isc.OBViewForm.create({
                fields: [{
                  name: 'active',
                  title: 'Active',
                  type: '_id_20',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'IsActive',
                  inpColumnName: 'inpisactive',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": 1,
                  "overflow": "visible",
                  dummy: ''
                }, {
                  name: 'priceAdjustment',
                  title: 'Price Adjustment',
                  type: '_id_19',
                  disabled: false,
                  readonly: false,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'M_Offer_ID',
                  inpColumnName: 'inpmOfferId',
                  referencedKeyColumnName: 'M_Offer_ID',
                  targetEntity: 'PricingAdjustment',
                  required: true,
                  width: '*',
                  dummy: ''
                }, {
                  name: 'lineNo',
                  title: 'Line No.',
                  type: '_id_11',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'Line',
                  inpColumnName: 'inpline',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: true,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'priceAdjustmentAmt',
                  title: 'Price Adjustment Amt.',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'Amtoffer',
                  inpColumnName: 'inpamtoffer',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: true,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'adjustedPrice',
                  title: 'Base Net Unit Price',
                  type: '_id_800008',
                  disabled: false,
                  readonly: false,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'Priceoffer',
                  inpColumnName: 'inppriceoffer',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: true,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: '1000100001',
                  title: 'Audit',
                  type: 'OBAuditSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'Audit',
                  itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
                  dummy: ''
                }, {
                  name: 'creationDate',
                  title: 'Creation Date',
                  type: '_id_16',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'createdBy',
                  title: 'Created By',
                  type: '_id_30',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: 'User',
                  required: false,
                  displayField: 'createdBy._identifier',
                  valueField: 'createdBy',
                  showPickerIcon: true,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'updated',
                  title: 'Updated',
                  type: '_id_16',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'updatedBy',
                  title: 'Updated By',
                  type: '_id_30',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: 'User',
                  required: false,
                  displayField: 'updatedBy._identifier',
                  valueField: 'updatedBy',
                  showPickerIcon: true,
                  'width': '*',
                  dummy: ''
                }, {
                  name: '_notes_',
                  title: 'dummy',
                  type: 'OBNoteSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_notes_Canvas'],
                  dummy: ''
                }, {
                  name: '_notes_Canvas',
                  title: 'dummy',
                  type: 'OBNoteCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }, {
                  name: '_linkedItems_',
                  title: 'dummy',
                  type: 'OBLinkedItemSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_linkedItems_Canvas'],
                  dummy: ''
                }, {
                  name: '_linkedItems_Canvas',
                  title: 'dummy',
                  type: 'OBLinkedItemCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }, {
                  name: '_attachments_',
                  title: 'dummy',
                  type: 'OBAttachmentsSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_attachments_Canvas'],
                  dummy: ''
                }, {
                  name: '_attachments_Canvas',
                  title: '',
                  type: 'OBAttachmentCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }],
                statusBarFields: [],
                obFormProperties: {
                  onFieldChanged: function (form, item, value) {
                    var f = form || this,
                        context = this.view.getContextInfo(false, true),
                        currentValues = f.view.getCurrentValues(),
                        otherItem;
                  }
                }
              });
              this.viewGrid = isc.OBViewGrid.create({
                uiPattern: 'RO',
                fields: [{
                  autoExpand: false,
                  type: '_id_20',
                  editorProperties: {
                    "width": 1,
                    "overflow": "visible",
                    "showTitle": false,
                    "showLabel": false,
                    columnName: 'IsActive',
                    inpColumnName: 'inpisactive',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'active',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBCheckboxItem',
                  width: '*',
                  autoFitWidth: false,
                  formatCellValue: function (value, record, rowNum, colNum, grid) {
                    return OB.Utilities.getYesNoDisplayValue(value);
                  },
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem',
                  title: 'Active',
                  prompt: 'Active',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'IsActive',
                  inpColumnName: 'inpisactive',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_19',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'M_Offer_ID',
                    inpColumnName: 'inpmOfferId',
                    referencedKeyColumnName: 'M_Offer_ID',
                    targetEntity: 'PricingAdjustment',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(44),
                  displayField: 'priceAdjustment._identifier',
                  valueField: 'priceAdjustment',
                  foreignKeyField: true,
                  name: 'priceAdjustment',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'priceAdjustment')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Price Adjustment',
                  prompt: 'Price Adjustment',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'M_Offer_ID',
                  inpColumnName: 'inpmOfferId',
                  referencedKeyColumnName: 'M_Offer_ID',
                  targetEntity: 'PricingAdjustment'
                }, {
                  autoExpand: false,
                  type: '_id_11',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Line',
                    inpColumnName: 'inpline',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'lineNo',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Line No.',
                  prompt: 'Line No.',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Line',
                  inpColumnName: 'inpline',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Amtoffer',
                    inpColumnName: 'inpamtoffer',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  name: 'priceAdjustmentAmt',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Price Adjustment Amt.',
                  prompt: 'Price Adjustment Amt.',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Amtoffer',
                  inpColumnName: 'inpamtoffer',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_800008',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Priceoffer',
                    inpColumnName: 'inppriceoffer',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  name: 'adjustedPrice',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Base Net Unit Price',
                  prompt: 'Base Net Unit Price',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Priceoffer',
                  inpColumnName: 'inppriceoffer',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_19',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'AD_Client_ID',
                    inpColumnName: 'inpadClientId',
                    referencedKeyColumnName: 'AD_Client_ID',
                    targetEntity: 'ADClient',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(44),
                  displayField: 'client._identifier',
                  valueField: 'client',
                  foreignKeyField: true,
                  name: 'client',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Client',
                  prompt: 'Client',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'AD_Client_ID',
                  inpColumnName: 'inpadClientId',
                  referencedKeyColumnName: 'AD_Client_ID',
                  targetEntity: 'ADClient'
                }, {
                  autoExpand: true,
                  type: '_id_19',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'AD_Org_ID',
                    inpColumnName: 'inpadOrgId',
                    referencedKeyColumnName: 'AD_Org_ID',
                    targetEntity: 'Organization',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(44),
                  displayField: 'organization._identifier',
                  valueField: 'organization',
                  foreignKeyField: true,
                  name: 'organization',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Organization',
                  prompt: 'Organization',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'AD_Org_ID',
                  inpColumnName: 'inpadOrgId',
                  referencedKeyColumnName: 'AD_Org_ID',
                  targetEntity: 'Organization'
                }, {
                  autoExpand: false,
                  type: '_id_16',
                  editorProperties: {
                    width: '*',
                    columnName: 'creationDate',
                    targetEntity: '',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'creationDate',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterEditorType: 'OBMiniDateRangeItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Creation Date',
                  prompt: 'Creation Date',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'creationDate',
                  inpColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'createdBy',
                    targetEntity: 'User',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'createdBy',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  filterEditorType: 'OBFKFilterTextItem',
                  displayField: 'createdBy._identifier',
                  valueField: 'createdBy',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Created By',
                  prompt: 'Created By',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'createdBy',
                  inpColumnName: '',
                  targetEntity: 'User'
                }, {
                  autoExpand: false,
                  type: '_id_16',
                  editorProperties: {
                    width: '*',
                    columnName: 'updated',
                    targetEntity: '',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'updated',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterEditorType: 'OBMiniDateRangeItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Updated',
                  prompt: 'Updated',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'updated',
                  inpColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'updatedBy',
                    targetEntity: 'User',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'updatedBy',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  filterEditorType: 'OBFKFilterTextItem',
                  displayField: 'updatedBy._identifier',
                  valueField: 'updatedBy',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Updated By',
                  prompt: 'Updated By',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'updatedBy',
                  inpColumnName: '',
                  targetEntity: 'User'
                }],
                autoExpandFieldNames: ['priceAdjustment', 'client', 'organization'],
                whereClause: '',
                orderByClause: '',
                sortField: 'lineNo',
                filterClause: '',
                filterName: '',
                foreignKeyFieldNames: ['priceAdjustment', 'client', 'organization']
              });
              this.Super('initWidget', arguments);
            },
            createViewStructure: function () {}
          }));
          this.addChildView(isc.OBStandardView.create({
            tabTitle: 'Line Tax',
            entity: 'OrderLineTax',
            parentProperty: 'salesOrderLine',
            tabId: '25C70617A7964B479BDA71197E7E88E9',
            moduleId: '0',
            defaultEditMode: false,
            mapping250: '/SalesOrder/LineTax',
            isAcctTab: false,
            isTrlTab: false,
            standardProperties: {
              inpTabId: '25C70617A7964B479BDA71197E7E88E9',
              inpwindowId: '143',
              inpTableId: 'E42DDB42FF0B4F82B1CF3C711B3F0DC0',
              inpkeyColumnId: 'C_Orderlinetax_ID',
              inpKeyName: 'inpcOrderlinetaxId'
            },
            propertyToColumns: [{
              property: 'lineNo',
              inpColumn: 'inpline',
              dbColumn: 'Line',
              sessionProperty: false,
              type: '_id_11'
            }, {
              property: 'tax',
              inpColumn: 'inpcTaxId',
              dbColumn: 'C_Tax_ID',
              sessionProperty: false,
              type: '_id_19'
            }, {
              property: 'taxableAmount',
              inpColumn: 'inptaxbaseamt',
              dbColumn: 'Taxbaseamt',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'taxAmount',
              inpColumn: 'inptaxamt',
              dbColumn: 'Taxamt',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'client',
              inpColumn: 'inpadClientId',
              dbColumn: 'AD_Client_ID',
              sessionProperty: true,
              type: '_id_19'
            }, {
              property: 'organization',
              inpColumn: 'inpadOrgId',
              dbColumn: 'AD_Org_ID',
              sessionProperty: true,
              type: '_id_19'
            }, {
              property: 'salesOrderLine',
              inpColumn: 'inpcOrderlineId',
              dbColumn: 'C_Orderline_ID',
              sessionProperty: false,
              type: '_id_30'
            }, {
              property: 'id',
              inpColumn: 'inpcOrderlinetaxId',
              dbColumn: 'C_Orderlinetax_ID',
              sessionProperty: false,
              type: '_id_13'
            }, {
              property: 'active',
              inpColumn: 'inpisactive',
              dbColumn: 'Isactive',
              sessionProperty: false,
              type: '_id_20'
            }, {
              property: 'id',
              inpColumn: 'C_Orderlinetax_ID',
              dbColumn: 'C_Orderlinetax_ID',
              sessionProperty: true,
              type: '_id_13'
            }],
            actionToolbarButtons: [],
            showParentButtons: true,
            buttonsHaveSessionLogic: false,
            iconToolbarButtons: [],
            initWidget: function () {
              this.dataSource = OB.Datasource.create({
                createClassName: 'OBViewDataSource',
                titleField: OB.Constants.IDENTIFIER,
                dataURL: '/openbravo/org.openbravo.service.datasource/OrderLineTax',
                recordXPath: '/response/data',
                dataFormat: 'json',
                operationBindings: [{
                  operationType: 'fetch',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'POST'
                  }
                }, {
                  operationType: 'add',
                  dataProtocol: 'postMessage'
                }, {
                  operationType: 'remove',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'DELETE'
                  }
                }, {
                  operationType: 'update',
                  dataProtocol: 'postMessage',
                  requestProperties: {
                    httpMethod: 'PUT'
                  }
                }],
                requestProperties: {
                  params: {
                    _className: 'OBViewDataSource'
                  }
                },
                fields: [{
                  name: 'id',
                  type: '_id_13',
                  additional: false,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'id'
                }, {
                  name: 'salesOrderLine',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'salesOrderLine',
                  hidden: true
                }, {
                  name: 'salesOrderLine._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'salesOrderLine'
                }, {
                  name: 'tax',
                  type: '_id_19',
                  additional: false,
                  required: true,
                  title: 'tax',
                  hidden: true
                }, {
                  name: 'tax._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'tax'
                }, {
                  name: 'client',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'client',
                  hidden: true
                }, {
                  name: 'client._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'client'
                }, {
                  name: 'organization',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'organization',
                  hidden: true
                }, {
                  name: 'organization._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'organization'
                }, {
                  name: 'active',
                  type: '_id_20',
                  additional: false,
                  title: 'active'
                }, {
                  name: 'creationDate',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'creationDate'
                }, {
                  name: 'createdBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'createdBy',
                  hidden: true
                }, {
                  name: 'createdBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'createdBy'
                }, {
                  name: 'updated',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'updated'
                }, {
                  name: 'updatedBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'updatedBy',
                  hidden: true
                }, {
                  name: 'updatedBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'updatedBy'
                }, {
                  name: 'taxableAmount',
                  type: '_id_12',
                  additional: false,
                  required: true,
                  title: 'taxableAmount'
                }, {
                  name: 'taxAmount',
                  type: '_id_12',
                  additional: false,
                  required: true,
                  title: 'taxAmount'
                }, {
                  name: 'lineNo',
                  type: '_id_11',
                  additional: false,
                  title: 'lineNo'
                }, {
                  name: 'salesOrder',
                  type: '_id_19',
                  additional: false,
                  canSave: false,
                  title: 'salesOrder',
                  hidden: true
                }, {
                  name: 'salesOrder._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'salesOrder'
                }]
              });
              this.viewForm = isc.OBViewForm.create({
                fields: [{
                  name: 'lineNo',
                  title: 'Line No.',
                  type: '_id_11',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'Line',
                  inpColumnName: 'inpline',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'tax',
                  title: 'Tax',
                  type: '_id_19',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'C_Tax_ID',
                  inpColumnName: 'inpcTaxId',
                  referencedKeyColumnName: 'C_Tax_ID',
                  targetEntity: 'FinancialMgmtTaxRate',
                  required: true,
                  width: '*',
                  dummy: ''
                }, {
                  name: 'taxableAmount',
                  title: 'Taxable Amount',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'Taxbaseamt',
                  inpColumnName: 'inptaxbaseamt',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: true,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'taxAmount',
                  title: 'Tax Amount',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: true,
                  columnName: 'Taxamt',
                  inpColumnName: 'inptaxamt',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: true,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: '1000100001',
                  title: 'Audit',
                  type: 'OBAuditSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'Audit',
                  itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
                  dummy: ''
                }, {
                  name: 'creationDate',
                  title: 'Creation Date',
                  type: '_id_16',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'createdBy',
                  title: 'Created By',
                  type: '_id_30',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: 'User',
                  required: false,
                  displayField: 'createdBy._identifier',
                  valueField: 'createdBy',
                  showPickerIcon: true,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'updated',
                  title: 'Updated',
                  type: '_id_16',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'updatedBy',
                  title: 'Updated By',
                  type: '_id_30',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: 'User',
                  required: false,
                  displayField: 'updatedBy._identifier',
                  valueField: 'updatedBy',
                  showPickerIcon: true,
                  'width': '*',
                  dummy: ''
                }, {
                  name: '_notes_',
                  title: 'dummy',
                  type: 'OBNoteSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_notes_Canvas'],
                  dummy: ''
                }, {
                  name: '_notes_Canvas',
                  title: 'dummy',
                  type: 'OBNoteCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }, {
                  name: '_linkedItems_',
                  title: 'dummy',
                  type: 'OBLinkedItemSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_linkedItems_Canvas'],
                  dummy: ''
                }, {
                  name: '_linkedItems_Canvas',
                  title: 'dummy',
                  type: 'OBLinkedItemCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }, {
                  name: '_attachments_',
                  title: 'dummy',
                  type: 'OBAttachmentsSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_attachments_Canvas'],
                  dummy: ''
                }, {
                  name: '_attachments_Canvas',
                  title: '',
                  type: 'OBAttachmentCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }],
                statusBarFields: [],
                obFormProperties: {
                  onFieldChanged: function (form, item, value) {
                    var f = form || this,
                        context = this.view.getContextInfo(false, true),
                        currentValues = f.view.getCurrentValues(),
                        otherItem;
                  }
                }
              });
              this.viewGrid = isc.OBViewGrid.create({
                uiPattern: 'RO',
                fields: [{
                  autoExpand: false,
                  type: '_id_11',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Line',
                    inpColumnName: 'inpline',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'lineNo',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Line No.',
                  prompt: 'Line No.',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Line',
                  inpColumnName: 'inpline',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_19',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'C_Tax_ID',
                    inpColumnName: 'inpcTaxId',
                    referencedKeyColumnName: 'C_Tax_ID',
                    targetEntity: 'FinancialMgmtTaxRate',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'tax._identifier',
                  valueField: 'tax',
                  foreignKeyField: true,
                  name: 'tax',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'tax')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Tax',
                  prompt: 'Tax',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'C_Tax_ID',
                  inpColumnName: 'inpcTaxId',
                  referencedKeyColumnName: 'C_Tax_ID',
                  targetEntity: 'FinancialMgmtTaxRate'
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Taxbaseamt',
                    inpColumnName: 'inptaxbaseamt',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'taxableAmount',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Taxable Amount',
                  prompt: 'Taxable Amount',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Taxbaseamt',
                  inpColumnName: 'inptaxbaseamt',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Taxamt',
                    inpColumnName: 'inptaxamt',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'taxAmount',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Tax Amount',
                  prompt: 'Tax Amount',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Taxamt',
                  inpColumnName: 'inptaxamt',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_19',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'AD_Client_ID',
                    inpColumnName: 'inpadClientId',
                    referencedKeyColumnName: 'AD_Client_ID',
                    targetEntity: 'ADClient',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'client._identifier',
                  valueField: 'client',
                  foreignKeyField: true,
                  name: 'client',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Client',
                  prompt: 'Client',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'AD_Client_ID',
                  inpColumnName: 'inpadClientId',
                  referencedKeyColumnName: 'AD_Client_ID',
                  targetEntity: 'ADClient'
                }, {
                  autoExpand: true,
                  type: '_id_19',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'AD_Org_ID',
                    inpColumnName: 'inpadOrgId',
                    referencedKeyColumnName: 'AD_Org_ID',
                    targetEntity: 'Organization',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'organization._identifier',
                  valueField: 'organization',
                  foreignKeyField: true,
                  name: 'organization',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Organization',
                  prompt: 'Organization',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'AD_Org_ID',
                  inpColumnName: 'inpadOrgId',
                  referencedKeyColumnName: 'AD_Org_ID',
                  targetEntity: 'Organization'
                }, {
                  autoExpand: false,
                  type: '_id_20',
                  editorProperties: {
                    "width": 1,
                    "overflow": "visible",
                    "showTitle": false,
                    "showLabel": false,
                    columnName: 'Isactive',
                    inpColumnName: 'inpisactive',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'active',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBCheckboxItem',
                  width: '*',
                  autoFitWidth: false,
                  formatCellValue: function (value, record, rowNum, colNum, grid) {
                    return OB.Utilities.getYesNoDisplayValue(value);
                  },
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem',
                  title: 'Active',
                  prompt: 'Active',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Isactive',
                  inpColumnName: 'inpisactive',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_16',
                  editorProperties: {
                    width: '*',
                    columnName: 'creationDate',
                    targetEntity: '',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'creationDate',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterEditorType: 'OBMiniDateRangeItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Creation Date',
                  prompt: 'Creation Date',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'creationDate',
                  inpColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'createdBy',
                    targetEntity: 'User',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'createdBy',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  filterEditorType: 'OBFKFilterTextItem',
                  displayField: 'createdBy._identifier',
                  valueField: 'createdBy',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Created By',
                  prompt: 'Created By',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'createdBy',
                  inpColumnName: '',
                  targetEntity: 'User'
                }, {
                  autoExpand: false,
                  type: '_id_16',
                  editorProperties: {
                    width: '*',
                    columnName: 'updated',
                    targetEntity: '',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'updated',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterEditorType: 'OBMiniDateRangeItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Updated',
                  prompt: 'Updated',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'updated',
                  inpColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'updatedBy',
                    targetEntity: 'User',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'updatedBy',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  filterEditorType: 'OBFKFilterTextItem',
                  displayField: 'updatedBy._identifier',
                  valueField: 'updatedBy',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Updated By',
                  prompt: 'Updated By',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'updatedBy',
                  inpColumnName: '',
                  targetEntity: 'User'
                }],
                autoExpandFieldNames: ['tax', 'client', 'organization'],
                whereClause: '',
                orderByClause: 'lineNo',
                sortField: '',
                filterClause: '',
                filterName: '',
                foreignKeyFieldNames: ['tax', 'client', 'organization']
              });
              this.Super('initWidget', arguments);
            },
            createViewStructure: function () {}
          }));
        }
      }));
      this.addChildView(isc.OBStandardView.create({
        tabTitle: 'Discounts',
        entity: 'OrderDiscount',
        parentProperty: 'salesOrder',
        tabId: '1011100000',
        moduleId: '0',
        defaultEditMode: false,
        mapping250: '/SalesOrder/Discounts',
        isAcctTab: false,
        isTrlTab: false,
        standardProperties: {
          inpTabId: '1011100000',
          inpwindowId: '143',
          inpTableId: '1011100000',
          inpkeyColumnId: 'C_Order_Discount_ID',
          inpKeyName: 'inpcOrderDiscountId'
        },
        propertyToColumns: [{
          property: 'lineNo',
          inpColumn: 'inpline',
          dbColumn: 'Line',
          sessionProperty: false,
          type: '_id_11'
        }, {
          property: 'discount',
          inpColumn: 'inpcDiscountId',
          dbColumn: 'C_Discount_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'cascade',
          inpColumn: 'inpcascade',
          dbColumn: 'Cascade',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'active',
          inpColumn: 'inpisactive',
          dbColumn: 'Isactive',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'client',
          inpColumn: 'inpadClientId',
          dbColumn: 'AD_Client_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'organization',
          inpColumn: 'inpadOrgId',
          dbColumn: 'AD_Org_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'salesOrder',
          inpColumn: 'inpcOrderId',
          dbColumn: 'C_Order_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'id',
          inpColumn: 'inpcOrderDiscountId',
          dbColumn: 'C_Order_Discount_ID',
          sessionProperty: false,
          type: '_id_13'
        }, {
          property: 'id',
          inpColumn: 'C_Order_Discount_ID',
          dbColumn: 'C_Order_Discount_ID',
          sessionProperty: true,
          type: '_id_13'
        }],
        actionToolbarButtons: [],
        showParentButtons: true,
        buttonsHaveSessionLogic: false,
        iconToolbarButtons: [],
        initWidget: function () {
          this.dataSource = OB.Datasource.create({
            createClassName: 'OBViewDataSource',
            titleField: OB.Constants.IDENTIFIER,
            dataURL: '/openbravo/org.openbravo.service.datasource/OrderDiscount',
            recordXPath: '/response/data',
            dataFormat: 'json',
            operationBindings: [{
              operationType: 'fetch',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'POST'
              }
            }, {
              operationType: 'add',
              dataProtocol: 'postMessage'
            }, {
              operationType: 'remove',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'DELETE'
              }
            }, {
              operationType: 'update',
              dataProtocol: 'postMessage',
              requestProperties: {
                httpMethod: 'PUT'
              }
            }],
            requestProperties: {
              params: {
                _className: 'OBViewDataSource'
              }
            },
            fields: [{
              name: 'id',
              type: '_id_13',
              additional: false,
              hidden: true,
              primaryKey: true,
              canSave: false,
              title: 'id'
            }, {
              name: 'client',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'client',
              hidden: true
            }, {
              name: 'client._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'client'
            }, {
              name: 'organization',
              type: '_id_19',
              additional: false,
              required: true,
              title: 'organization',
              hidden: true
            }, {
              name: 'organization._identifier',
              type: 'text',
              hidden: true,
              title: 'organization'
            }, {
              name: 'active',
              type: '_id_20',
              additional: false,
              title: 'active'
            }, {
              name: 'creationDate',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'creationDate'
            }, {
              name: 'createdBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'createdBy',
              hidden: true
            }, {
              name: 'createdBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'createdBy'
            }, {
              name: 'updated',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'updated'
            }, {
              name: 'updatedBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'updatedBy',
              hidden: true
            }, {
              name: 'updatedBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'updatedBy'
            }, {
              name: 'salesOrder',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'salesOrder',
              hidden: true
            }, {
              name: 'salesOrder._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'salesOrder'
            }, {
              name: 'discount',
              type: '_id_19',
              additional: false,
              required: true,
              title: 'discount',
              hidden: true
            }, {
              name: 'discount._identifier',
              type: 'text',
              hidden: true,
              title: 'discount'
            }, {
              name: 'lineNo',
              type: '_id_11',
              additional: false,
              required: true,
              title: 'lineNo'
            }, {
              name: 'cascade',
              type: '_id_20',
              additional: false,
              title: 'cascade'
            }]
          });
          this.viewForm = isc.OBViewForm.create({
            fields: [{
              name: 'lineNo',
              title: 'Line No.',
              type: '_id_11',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Line',
              inpColumnName: 'inpline',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'discount',
              title: 'Discount',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Discount_ID',
              inpColumnName: 'inpcDiscountId',
              referencedKeyColumnName: 'C_Discount_ID',
              targetEntity: 'PricingDiscount',
              required: true,
              width: '*',
              dummy: ''
            }, {
              name: 'cascade',
              title: 'Cascade',
              type: '_id_20',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Cascade',
              inpColumnName: 'inpcascade',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: 'active',
              title: 'Active',
              type: '_id_20',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Isactive',
              inpColumnName: 'inpisactive',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: '1000100001',
              title: 'Audit',
              type: 'OBAuditSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'Audit',
              itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
              dummy: ''
            }, {
              name: 'creationDate',
              title: 'Creation Date',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'createdBy',
              title: 'Created By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: 'updated',
              title: 'Updated',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'updatedBy',
              title: 'Updated By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: '_notes_',
              title: 'dummy',
              type: 'OBNoteSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_notes_Canvas'],
              dummy: ''
            }, {
              name: '_notes_Canvas',
              title: 'dummy',
              type: 'OBNoteCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_linkedItems_',
              title: 'dummy',
              type: 'OBLinkedItemSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_linkedItems_Canvas'],
              dummy: ''
            }, {
              name: '_linkedItems_Canvas',
              title: 'dummy',
              type: 'OBLinkedItemCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_attachments_',
              title: 'dummy',
              type: 'OBAttachmentsSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_attachments_Canvas'],
              dummy: ''
            }, {
              name: '_attachments_Canvas',
              title: '',
              type: 'OBAttachmentCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }],
            statusBarFields: [],
            obFormProperties: {
              onFieldChanged: function (form, item, value) {
                var f = form || this,
                    context = this.view.getContextInfo(false, true),
                    currentValues = f.view.getCurrentValues(),
                    otherItem;
                otherItem = f.getItem('lineNo');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('discount');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('cascade');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('active');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (context.Processed === 'Y') {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
              }
            }
          });
          this.viewGrid = isc.OBViewGrid.create({
            uiPattern: 'STD',
            fields: [{
              autoExpand: false,
              type: '_id_11',
              editorProperties: {
                "width": "50%",
                columnName: 'Line',
                inpColumnName: 'inpline',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'lineNo',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Line No.',
              prompt: 'Line No.',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Line',
              inpColumnName: 'inpline',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Discount_ID',
                inpColumnName: 'inpcDiscountId',
                referencedKeyColumnName: 'C_Discount_ID',
                targetEntity: 'PricingDiscount',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(12),
              displayField: 'discount._identifier',
              valueField: 'discount',
              foreignKeyField: true,
              name: 'discount',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'discount')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Discount',
              prompt: 'Discount',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Discount_ID',
              inpColumnName: 'inpcDiscountId',
              referencedKeyColumnName: 'C_Discount_ID',
              targetEntity: 'PricingDiscount'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'Cascade',
                inpColumnName: 'inpcascade',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'cascade',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Cascade',
              prompt: 'Cascade',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Cascade',
              inpColumnName: 'inpcascade',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'Isactive',
                inpColumnName: 'inpisactive',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'active',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Active',
              prompt: 'Active',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Isactive',
              inpColumnName: 'inpisactive',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Client_ID',
                inpColumnName: 'inpadClientId',
                referencedKeyColumnName: 'AD_Client_ID',
                targetEntity: 'ADClient',
                disabled: false,
                readonly: false,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(12),
              displayField: 'client._identifier',
              valueField: 'client',
              foreignKeyField: true,
              name: 'client',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Client',
              prompt: 'Client',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Client_ID',
              inpColumnName: 'inpadClientId',
              referencedKeyColumnName: 'AD_Client_ID',
              targetEntity: 'ADClient'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Org_ID',
                inpColumnName: 'inpadOrgId',
                referencedKeyColumnName: 'AD_Org_ID',
                targetEntity: 'Organization',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(12),
              displayField: 'organization._identifier',
              valueField: 'organization',
              foreignKeyField: true,
              name: 'organization',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Organization',
              prompt: 'Organization',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Org_ID',
              inpColumnName: 'inpadOrgId',
              referencedKeyColumnName: 'AD_Org_ID',
              targetEntity: 'Organization'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'creationDate',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'creationDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Creation Date',
              prompt: 'Creation Date',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'creationDate',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'createdBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'createdBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Created By',
              prompt: 'Created By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'createdBy',
              inpColumnName: '',
              targetEntity: 'User'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'updated',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updated',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated',
              prompt: 'Updated',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updated',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'updatedBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updatedBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated By',
              prompt: 'Updated By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updatedBy',
              inpColumnName: '',
              targetEntity: 'User'
            }],
            autoExpandFieldNames: ['discount', 'client', 'organization'],
            whereClause: '',
            orderByClause: '',
            sortField: 'lineNo',
            filterClause: '',
            filterName: '',
            foreignKeyFieldNames: ['discount', 'client', 'organization']
          });
          this.Super('initWidget', arguments);
        },
        createViewStructure: function () {}
      }));
      this.addChildView(isc.OBStandardView.create({
        tabTitle: 'Tax',
        entity: 'OrderTax',
        parentProperty: 'salesOrder',
        tabId: '236',
        moduleId: '0',
        defaultEditMode: false,
        mapping250: '/SalesOrder/Tax',
        isAcctTab: false,
        isTrlTab: false,
        standardProperties: {
          inpTabId: '236',
          inpwindowId: '143',
          inpTableId: '314',
          inpkeyColumnId: 'C_OrderTax_ID',
          inpKeyName: 'inpcOrdertaxId'
        },
        propertyToColumns: [{
          property: 'lineNo',
          inpColumn: 'inpline',
          dbColumn: 'Line',
          sessionProperty: false,
          type: '_id_11'
        }, {
          property: 'tax',
          inpColumn: 'inpcTaxId',
          dbColumn: 'C_Tax_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'taxAmount',
          inpColumn: 'inptaxamt',
          dbColumn: 'TaxAmt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'taxableAmount',
          inpColumn: 'inptaxbaseamt',
          dbColumn: 'TaxBaseAmt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'id',
          inpColumn: 'inpcOrdertaxId',
          dbColumn: 'C_OrderTax_ID',
          sessionProperty: false,
          type: '_id_13'
        }, {
          property: 'salesOrder',
          inpColumn: 'inpcOrderId',
          dbColumn: 'C_Order_ID',
          sessionProperty: true,
          type: '_id_800062'
        }, {
          property: 'organization',
          inpColumn: 'inpadOrgId',
          dbColumn: 'AD_Org_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'client',
          inpColumn: 'inpadClientId',
          dbColumn: 'AD_Client_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'active',
          inpColumn: 'inpisactive',
          dbColumn: 'IsActive',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'id',
          inpColumn: 'C_OrderTax_ID',
          dbColumn: 'C_OrderTax_ID',
          sessionProperty: true,
          type: '_id_13'
        }],
        actionToolbarButtons: [],
        showParentButtons: true,
        buttonsHaveSessionLogic: false,
        iconToolbarButtons: [],
        initWidget: function () {
          this.dataSource = OB.Datasource.create({
            createClassName: 'OBViewDataSource',
            titleField: OB.Constants.IDENTIFIER,
            dataURL: '/openbravo/org.openbravo.service.datasource/OrderTax',
            recordXPath: '/response/data',
            dataFormat: 'json',
            operationBindings: [{
              operationType: 'fetch',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'POST'
              }
            }, {
              operationType: 'add',
              dataProtocol: 'postMessage'
            }, {
              operationType: 'remove',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'DELETE'
              }
            }, {
              operationType: 'update',
              dataProtocol: 'postMessage',
              requestProperties: {
                httpMethod: 'PUT'
              }
            }],
            requestProperties: {
              params: {
                _className: 'OBViewDataSource'
              }
            },
            fields: [{
              name: 'id',
              type: '_id_13',
              additional: false,
              hidden: true,
              primaryKey: true,
              required: true,
              title: 'id'
            }, {
              name: 'salesOrder',
              type: '_id_800062',
              additional: false,
              canSave: false,
              title: 'salesOrder',
              hidden: true
            }, {
              name: 'salesOrder._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'salesOrder'
            }, {
              name: 'tax',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'tax',
              hidden: true
            }, {
              name: 'tax._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'tax'
            }, {
              name: 'client',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'client',
              hidden: true
            }, {
              name: 'client._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'client'
            }, {
              name: 'organization',
              type: '_id_19',
              additional: false,
              required: true,
              title: 'organization',
              hidden: true
            }, {
              name: 'organization._identifier',
              type: 'text',
              hidden: true,
              title: 'organization'
            }, {
              name: 'active',
              type: '_id_20',
              additional: false,
              title: 'active'
            }, {
              name: 'creationDate',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'creationDate'
            }, {
              name: 'createdBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'createdBy',
              hidden: true
            }, {
              name: 'createdBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'createdBy'
            }, {
              name: 'updated',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'updated'
            }, {
              name: 'updatedBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'updatedBy',
              hidden: true
            }, {
              name: 'updatedBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'updatedBy'
            }, {
              name: 'taxableAmount',
              type: '_id_12',
              additional: false,
              canSave: false,
              title: 'taxableAmount'
            }, {
              name: 'taxAmount',
              type: '_id_12',
              additional: false,
              canSave: false,
              title: 'taxAmount'
            }, {
              name: 'lineNo',
              type: '_id_11',
              additional: false,
              title: 'lineNo'
            }]
          });
          this.viewForm = isc.OBViewForm.create({
            fields: [{
              name: 'lineNo',
              title: 'Line No.',
              type: '_id_11',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Line',
              inpColumnName: 'inpline',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": "50%",
              dummy: ''
            }, {
              name: 'tax',
              title: 'Tax',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Tax_ID',
              inpColumnName: 'inpcTaxId',
              referencedKeyColumnName: 'C_Tax_ID',
              targetEntity: 'FinancialMgmtTaxRate',
              required: true,
              firstFocusedField: true,
              width: '*',
              dummy: ''
            }, {
              name: 'taxAmount',
              title: 'Tax Amount',
              type: '_id_12',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'TaxAmt',
              inpColumnName: 'inptaxamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'taxableAmount',
              title: 'Taxable Amount',
              type: '_id_12',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'TaxBaseAmt',
              inpColumnName: 'inptaxbaseamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: '1000100001',
              title: 'Audit',
              type: 'OBAuditSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'Audit',
              itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
              dummy: ''
            }, {
              name: 'creationDate',
              title: 'Creation Date',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'createdBy',
              title: 'Created By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: 'updated',
              title: 'Updated',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'updatedBy',
              title: 'Updated By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: '_notes_',
              title: 'dummy',
              type: 'OBNoteSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_notes_Canvas'],
              dummy: ''
            }, {
              name: '_notes_Canvas',
              title: 'dummy',
              type: 'OBNoteCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_linkedItems_',
              title: 'dummy',
              type: 'OBLinkedItemSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_linkedItems_Canvas'],
              dummy: ''
            }, {
              name: '_linkedItems_Canvas',
              title: 'dummy',
              type: 'OBLinkedItemCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_attachments_',
              title: 'dummy',
              type: 'OBAttachmentsSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_attachments_Canvas'],
              dummy: ''
            }, {
              name: '_attachments_Canvas',
              title: '',
              type: 'OBAttachmentCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }],
            statusBarFields: [],
            obFormProperties: {
              onFieldChanged: function (form, item, value) {
                var f = form || this,
                    context = this.view.getContextInfo(false, true),
                    currentValues = f.view.getCurrentValues(),
                    otherItem;
              }
            }
          });
          this.viewGrid = isc.OBViewGrid.create({
            uiPattern: 'STD',
            fields: [{
              autoExpand: false,
              type: '_id_11',
              editorProperties: {
                "width": "50%",
                columnName: 'Line',
                inpColumnName: 'inpline',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'lineNo',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Line No.',
              prompt: 'Line No.',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Line',
              inpColumnName: 'inpline',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Tax_ID',
                inpColumnName: 'inpcTaxId',
                referencedKeyColumnName: 'C_Tax_ID',
                targetEntity: 'FinancialMgmtTaxRate',
                disabled: false,
                readonly: false,
                updatable: false,
                firstFocusedField: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'tax._identifier',
              valueField: 'tax',
              foreignKeyField: true,
              name: 'tax',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'tax')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Tax',
              prompt: 'Tax',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Tax_ID',
              inpColumnName: 'inpcTaxId',
              referencedKeyColumnName: 'C_Tax_ID',
              targetEntity: 'FinancialMgmtTaxRate'
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'TaxAmt',
                inpColumnName: 'inptaxamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false
              },
              name: 'taxAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Tax Amount',
              prompt: 'Tax Amount',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'TaxAmt',
              inpColumnName: 'inptaxamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'TaxBaseAmt',
                inpColumnName: 'inptaxbaseamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: false
              },
              name: 'taxableAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Taxable Amount',
              prompt: 'Taxable Amount',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'TaxBaseAmt',
              inpColumnName: 'inptaxbaseamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsActive',
                inpColumnName: 'inpisactive',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'active',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Active',
              prompt: 'Active',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsActive',
              inpColumnName: 'inpisactive',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Client_ID',
                inpColumnName: 'inpadClientId',
                referencedKeyColumnName: 'AD_Client_ID',
                targetEntity: 'ADClient',
                disabled: false,
                readonly: false,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'client._identifier',
              valueField: 'client',
              foreignKeyField: true,
              name: 'client',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Client',
              prompt: 'Client',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Client_ID',
              inpColumnName: 'inpadClientId',
              referencedKeyColumnName: 'AD_Client_ID',
              targetEntity: 'ADClient'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Org_ID',
                inpColumnName: 'inpadOrgId',
                referencedKeyColumnName: 'AD_Org_ID',
                targetEntity: 'Organization',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'organization._identifier',
              valueField: 'organization',
              foreignKeyField: true,
              name: 'organization',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Organization',
              prompt: 'Organization',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Org_ID',
              inpColumnName: 'inpadOrgId',
              referencedKeyColumnName: 'AD_Org_ID',
              targetEntity: 'Organization'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'creationDate',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'creationDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Creation Date',
              prompt: 'Creation Date',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'creationDate',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'createdBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'createdBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Created By',
              prompt: 'Created By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'createdBy',
              inpColumnName: '',
              targetEntity: 'User'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'updated',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updated',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated',
              prompt: 'Updated',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updated',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'updatedBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updatedBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated By',
              prompt: 'Updated By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updatedBy',
              inpColumnName: '',
              targetEntity: 'User'
            }],
            autoExpandFieldNames: ['tax', 'client', 'organization'],
            whereClause: '',
            orderByClause: 'lineNo',
            sortField: '',
            filterClause: '',
            filterName: '',
            foreignKeyFieldNames: ['tax', 'client', 'organization']
          });
          this.Super('initWidget', arguments);
        },
        createViewStructure: function () {}
      }));
      this.addChildView(isc.OBStandardView.create({
        tabTitle: 'Payment In Plan',
        entity: 'FIN_Payment_Sched_Ord_V',
        parentProperty: 'salesOrder',
        tabId: 'EB0E0C5A58344F7FA345097E7365CD22',
        moduleId: 'A918E3331C404B889D69AA9BFAFB23AC',
        defaultEditMode: false,
        mapping250: '/SalesOrder/PaymentInPlanEB0E0C5A58344F7FA345097E7365CD22',
        isAcctTab: false,
        isTrlTab: false,
        standardProperties: {
          inpTabId: 'EB0E0C5A58344F7FA345097E7365CD22',
          inpwindowId: '143',
          inpTableId: '70E57DEA195843729FF303C9A71EBCA3',
          inpkeyColumnId: 'Fin_Payment_Sched_Ord_V_ID',
          inpKeyName: 'inpfinPaymentSchedOrdVId'
        },
        propertyToColumns: [{
          property: 'dueDate',
          inpColumn: 'inpduedate',
          dbColumn: 'Duedate',
          sessionProperty: true,
          type: '_id_15'
        }, {
          property: 'paymentMethod',
          inpColumn: 'inpfinPaymentmethodId',
          dbColumn: 'FIN_Paymentmethod_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'expected',
          inpColumn: 'inpexpected',
          dbColumn: 'Expected',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'received',
          inpColumn: 'inpreceived',
          dbColumn: 'Received',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'outstanding',
          inpColumn: 'inpoutstanding',
          dbColumn: 'Outstanding',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'lastPayment',
          inpColumn: 'inplastpayment',
          dbColumn: 'Lastpayment',
          sessionProperty: false,
          type: '_id_15'
        }, {
          property: 'numberOfPayments',
          inpColumn: 'inpnumberofpayments',
          dbColumn: 'Numberofpayments',
          sessionProperty: false,
          type: '_id_11'
        }, {
          property: 'currency',
          inpColumn: 'inpcCurrencyId',
          dbColumn: 'C_Currency_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'client',
          inpColumn: 'inpadClientId',
          dbColumn: 'AD_Client_ID',
          sessionProperty: true,
          type: '_id_30'
        }, {
          property: 'invoice',
          inpColumn: 'inpcInvoiceId',
          dbColumn: 'C_Invoice_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'salesOrder',
          inpColumn: 'inpcOrderId',
          dbColumn: 'C_Order_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'active',
          inpColumn: 'inpisactive',
          dbColumn: 'Isactive',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'organization',
          inpColumn: 'inpadOrgId',
          dbColumn: 'AD_Org_ID',
          sessionProperty: true,
          type: '_id_30'
        }, {
          property: 'id',
          inpColumn: 'inpfinPaymentSchedOrdVId',
          dbColumn: 'Fin_Payment_Sched_Ord_V_ID',
          sessionProperty: false,
          type: '_id_13'
        }, {
          property: 'id',
          inpColumn: 'Fin_Payment_Sched_Ord_V_ID',
          dbColumn: 'Fin_Payment_Sched_Ord_V_ID',
          sessionProperty: true,
          type: '_id_13'
        }],
        actionToolbarButtons: [],
        showParentButtons: true,
        buttonsHaveSessionLogic: false,
        iconToolbarButtons: [],
        hasChildTabs: true,
        initWidget: function () {
          this.dataSource = OB.Datasource.create({
            createClassName: 'OBViewDataSource',
            titleField: OB.Constants.IDENTIFIER,
            dataURL: '/openbravo/org.openbravo.service.datasource/FIN_Payment_Sched_Ord_V',
            recordXPath: '/response/data',
            dataFormat: 'json',
            operationBindings: [{
              operationType: 'fetch',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'POST'
              }
            }, {
              operationType: 'add',
              dataProtocol: 'postMessage'
            }, {
              operationType: 'remove',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'DELETE'
              }
            }, {
              operationType: 'update',
              dataProtocol: 'postMessage',
              requestProperties: {
                httpMethod: 'PUT'
              }
            }],
            requestProperties: {
              params: {
                _className: 'OBViewDataSource'
              }
            },
            fields: [{
              name: 'id',
              type: '_id_13',
              additional: false,
              hidden: true,
              primaryKey: true,
              canSave: false,
              title: 'id'
            }, {
              name: 'client',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'client',
              hidden: true
            }, {
              name: 'client._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'client'
            }, {
              name: 'organization',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'organization',
              hidden: true
            }, {
              name: 'organization._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'organization'
            }, {
              name: 'active',
              type: '_id_20',
              additional: false,
              title: 'active'
            }, {
              name: 'creationDate',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'creationDate'
            }, {
              name: 'createdBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'createdBy',
              hidden: true
            }, {
              name: 'createdBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'createdBy'
            }, {
              name: 'updated',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'updated'
            }, {
              name: 'updatedBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'updatedBy',
              hidden: true
            }, {
              name: 'updatedBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'updatedBy'
            }, {
              name: 'invoice',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'invoice',
              hidden: true
            }, {
              name: 'invoice._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'invoice'
            }, {
              name: 'salesOrder',
              type: '_id_30',
              additional: false,
              title: 'salesOrder',
              hidden: true
            }, {
              name: 'salesOrder._identifier',
              type: 'text',
              hidden: true,
              title: 'salesOrder'
            }, {
              name: 'dueDate',
              type: '_id_15',
              additional: false,
              title: 'dueDate'
            }, {
              name: 'paymentMethod',
              type: '_id_30',
              additional: false,
              required: true,
              title: 'paymentMethod',
              hidden: true
            }, {
              name: 'paymentMethod._identifier',
              type: 'text',
              hidden: true,
              title: 'paymentMethod'
            }, {
              name: 'expected',
              type: '_id_12',
              additional: false,
              required: true,
              title: 'expected'
            }, {
              name: 'received',
              type: '_id_12',
              additional: false,
              required: true,
              title: 'received'
            }, {
              name: 'outstanding',
              type: '_id_12',
              additional: false,
              required: true,
              title: 'outstanding'
            }, {
              name: 'currency',
              type: '_id_30',
              additional: false,
              required: true,
              title: 'currency',
              hidden: true
            }, {
              name: 'currency._identifier',
              type: 'text',
              hidden: true,
              title: 'currency'
            }, {
              name: 'lastPayment',
              type: '_id_15',
              additional: false,
              title: 'lastPayment'
            }, {
              name: 'numberOfPayments',
              type: '_id_11',
              additional: false,
              title: 'numberOfPayments'
            }, {
              name: 'fINPaymentPriority',
              type: '_id_30',
              additional: false,
              title: 'fINPaymentPriority',
              hidden: true
            }, {
              name: 'fINPaymentPriority._identifier',
              type: 'text',
              hidden: true,
              title: 'fINPaymentPriority'
            }, {
              name: 'updatePaymentPlan',
              type: '_id_28',
              additional: false,
              title: 'updatePaymentPlan'
            }]
          });
          this.viewForm = isc.OBViewForm.create({
            fields: [{
              name: 'dueDate',
              title: 'Due Date',
              type: '_id_15',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Duedate',
              inpColumnName: 'inpduedate',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": "50%",
              dummy: ''
            }, {
              name: 'paymentMethod',
              title: 'Payment Method',
              type: '_id_30',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'FIN_Paymentmethod_ID',
              inpColumnName: 'inpfinPaymentmethodId',
              referencedKeyColumnName: 'Fin_Paymentmethod_ID',
              targetEntity: 'FIN_PaymentMethod',
              required: true,
              displayField: 'paymentMethod._identifier',
              valueField: 'paymentMethod',
              showPickerIcon: true,
              width: '*',
              dummy: ''
            }, {
              name: 'expected',
              title: 'Expected Amount',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Expected',
              inpColumnName: 'inpexpected',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'received',
              title: 'Received',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Received',
              inpColumnName: 'inpreceived',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'outstanding',
              title: 'Outstanding',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Outstanding',
              inpColumnName: 'inpoutstanding',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'lastPayment',
              title: 'Last Payment In Date',
              type: '_id_15',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Lastpayment',
              inpColumnName: 'inplastpayment',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": "50%",
              dummy: ''
            }, {
              name: 'numberOfPayments',
              title: 'Number of Payments In',
              type: '_id_11',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Numberofpayments',
              inpColumnName: 'inpnumberofpayments',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": "50%",
              dummy: ''
            }, {
              name: 'currency',
              title: 'Currency',
              type: '_id_30',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Currency_ID',
              inpColumnName: 'inpcCurrencyId',
              referencedKeyColumnName: 'C_Currency_ID',
              targetEntity: 'Currency',
              required: true,
              displayField: 'currency._identifier',
              valueField: 'currency',
              showPickerIcon: true,
              width: '*',
              dummy: ''
            }, {
              name: '1000100001',
              title: 'Audit',
              type: 'OBAuditSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'Audit',
              itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
              dummy: ''
            }, {
              name: 'creationDate',
              title: 'Creation Date',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'createdBy',
              title: 'Created By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: 'updated',
              title: 'Updated',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'updatedBy',
              title: 'Updated By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: '_notes_',
              title: 'dummy',
              type: 'OBNoteSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_notes_Canvas'],
              dummy: ''
            }, {
              name: '_notes_Canvas',
              title: 'dummy',
              type: 'OBNoteCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_linkedItems_',
              title: 'dummy',
              type: 'OBLinkedItemSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_linkedItems_Canvas'],
              dummy: ''
            }, {
              name: '_linkedItems_Canvas',
              title: 'dummy',
              type: 'OBLinkedItemCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_attachments_',
              title: 'dummy',
              type: 'OBAttachmentsSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_attachments_Canvas'],
              dummy: ''
            }, {
              name: '_attachments_Canvas',
              title: '',
              type: 'OBAttachmentCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }],
            statusBarFields: [],
            obFormProperties: {
              onFieldChanged: function (form, item, value) {
                var f = form || this,
                    context = this.view.getContextInfo(false, true),
                    currentValues = f.view.getCurrentValues(),
                    otherItem;
              }
            }
          });
          this.viewGrid = isc.OBViewGrid.create({
            uiPattern: 'RO',
            fields: [{
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'Duedate',
                inpColumnName: 'inpduedate',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'dueDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Due Date',
              prompt: 'Due Date',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Duedate',
              inpColumnName: 'inpduedate',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'FIN_Paymentmethod_ID',
                inpColumnName: 'inpfinPaymentmethodId',
                referencedKeyColumnName: 'Fin_Paymentmethod_ID',
                targetEntity: 'FIN_PaymentMethod',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(32),
              displayField: 'paymentMethod._identifier',
              valueField: 'paymentMethod',
              foreignKeyField: true,
              name: 'paymentMethod',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'paymentMethod')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Payment Method',
              prompt: 'Payment Method',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'FIN_Paymentmethod_ID',
              inpColumnName: 'inpfinPaymentmethodId',
              referencedKeyColumnName: 'Fin_Paymentmethod_ID',
              targetEntity: 'FIN_PaymentMethod'
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'Expected',
                inpColumnName: 'inpexpected',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'expected',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Expected Amount',
              prompt: 'Expected Amount',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Expected',
              inpColumnName: 'inpexpected',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'Received',
                inpColumnName: 'inpreceived',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'received',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Received',
              prompt: 'Received',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Received',
              inpColumnName: 'inpreceived',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'Outstanding',
                inpColumnName: 'inpoutstanding',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'outstanding',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Outstanding',
              prompt: 'Outstanding',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Outstanding',
              inpColumnName: 'inpoutstanding',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'Lastpayment',
                inpColumnName: 'inplastpayment',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'lastPayment',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Last Payment In Date',
              prompt: 'Last Payment In Date',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Lastpayment',
              inpColumnName: 'inplastpayment',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_11',
              editorProperties: {
                "width": "50%",
                columnName: 'Numberofpayments',
                inpColumnName: 'inpnumberofpayments',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'numberOfPayments',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Number of Payments In',
              prompt: 'Number of Payments In',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Numberofpayments',
              inpColumnName: 'inpnumberofpayments',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'C_Currency_ID',
                inpColumnName: 'inpcCurrencyId',
                referencedKeyColumnName: 'C_Currency_ID',
                targetEntity: 'Currency',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(19),
              displayField: 'currency._identifier',
              valueField: 'currency',
              foreignKeyField: true,
              name: 'currency',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'currency')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Currency',
              prompt: 'Currency',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Currency_ID',
              inpColumnName: 'inpcCurrencyId',
              referencedKeyColumnName: 'C_Currency_ID',
              targetEntity: 'Currency'
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '',
                columnName: 'AD_Client_ID',
                inpColumnName: 'inpadClientId',
                referencedKeyColumnName: 'AD_Client_ID',
                targetEntity: 'ADClient',
                disabled: false,
                readonly: false,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(32),
              displayField: 'client._identifier',
              valueField: 'client',
              foreignKeyField: true,
              name: 'client',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Client',
              prompt: 'Client',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Client_ID',
              inpColumnName: 'inpadClientId',
              referencedKeyColumnName: 'AD_Client_ID',
              targetEntity: 'ADClient'
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '',
                columnName: 'C_Invoice_ID',
                inpColumnName: 'inpcInvoiceId',
                referencedKeyColumnName: 'C_Invoice_ID',
                targetEntity: 'Invoice',
                disabled: false,
                readonly: false,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(32),
              displayField: 'invoice._identifier',
              valueField: 'invoice',
              foreignKeyField: true,
              name: 'invoice',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'invoice')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Invoice',
              prompt: 'Invoice',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_Invoice_ID',
              inpColumnName: 'inpcInvoiceId',
              referencedKeyColumnName: 'C_Invoice_ID',
              targetEntity: 'Invoice'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'Isactive',
                inpColumnName: 'inpisactive',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'active',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Active',
              prompt: 'Active',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Isactive',
              inpColumnName: 'inpisactive',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '',
                columnName: 'AD_Org_ID',
                inpColumnName: 'inpadOrgId',
                referencedKeyColumnName: 'AD_Org_ID',
                targetEntity: 'Organization',
                disabled: false,
                readonly: false,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(32),
              displayField: 'organization._identifier',
              valueField: 'organization',
              foreignKeyField: true,
              name: 'organization',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Organization',
              prompt: 'Organization',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Org_ID',
              inpColumnName: 'inpadOrgId',
              referencedKeyColumnName: 'AD_Org_ID',
              targetEntity: 'Organization'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'creationDate',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'creationDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Creation Date',
              prompt: 'Creation Date',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'creationDate',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'createdBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'createdBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Created By',
              prompt: 'Created By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'createdBy',
              inpColumnName: '',
              targetEntity: 'User'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'updated',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updated',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated',
              prompt: 'Updated',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updated',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'updatedBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updatedBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated By',
              prompt: 'Updated By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updatedBy',
              inpColumnName: '',
              targetEntity: 'User'
            }],
            autoExpandFieldNames: ['paymentMethod', 'client', 'invoice', 'organization', 'currency'],
            whereClause: '',
            orderByClause: '',
            sortField: 'dueDate',
            filterClause: '',
            filterName: '',
            foreignKeyFieldNames: ['paymentMethod', 'currency', 'client', 'invoice', 'organization']
          });
          this.Super('initWidget', arguments);
        },
        createViewStructure: function () {
          this.addChildView(isc.OBStandardView.create({
            tabTitle: 'Payment In Details',
            entity: 'FIN_Payment_Detail_V',
            parentProperty: 'orderPaymentPlan',
            tabId: 'B82C02920AA84E8DB57D553185BD2F06',
            moduleId: 'A918E3331C404B889D69AA9BFAFB23AC',
            defaultEditMode: false,
            mapping250: '/SalesOrder/PaymentInDetailsB82C02920AA84E8DB57D553185BD2F06',
            isAcctTab: false,
            isTrlTab: false,
            standardProperties: {
              inpTabId: 'B82C02920AA84E8DB57D553185BD2F06',
              inpwindowId: '143',
              inpTableId: 'DC63963AB3F1489BAAB5A9A7EFD1B407',
              inpkeyColumnId: 'Fin_Payment_Detail_V_ID',
              inpKeyName: 'inpfinPaymentDetailVId'
            },
            propertyToColumns: [{
              property: 'payment',
              inpColumn: 'inpfinPaymentId',
              dbColumn: 'FIN_Payment_ID',
              sessionProperty: false,
              type: '_id_30'
            }, {
              property: 'paymentDate',
              inpColumn: 'inppaymentdate',
              dbColumn: 'Paymentdate',
              sessionProperty: false,
              type: '_id_15'
            }, {
              property: 'dueDate',
              inpColumn: 'inpduedate',
              dbColumn: 'Duedate',
              sessionProperty: false,
              type: '_id_15'
            }, {
              property: 'paymentMethod',
              inpColumn: 'inpfinPaymentmethodId',
              dbColumn: 'Fin_Paymentmethod_ID',
              sessionProperty: false,
              type: '_id_30'
            }, {
              property: 'finFinancialAccount',
              inpColumn: 'inpfinFinancialAccountId',
              dbColumn: 'Fin_Financial_Account_ID',
              sessionProperty: false,
              type: '_id_30'
            }, {
              property: 'expected',
              inpColumn: 'inpexpected',
              dbColumn: 'Expected',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'paidAmount',
              inpColumn: 'inppaidamt',
              dbColumn: 'Paidamt',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'currency',
              inpColumn: 'inpcCurrencyId',
              dbColumn: 'C_Currency_ID',
              sessionProperty: false,
              type: '_id_30'
            }, {
              property: 'writeoffAmount',
              inpColumn: 'inpwriteoffamt',
              dbColumn: 'Writeoffamt',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'expectedConverted',
              inpColumn: 'inpexpectedconverted',
              dbColumn: 'ExpectedConverted',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'paidConverted',
              inpColumn: 'inppaidconverted',
              dbColumn: 'PaidConverted',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'finaccTxnConvertRate',
              inpColumn: 'inpfinaccTxnConvertRate',
              dbColumn: 'Finacc_Txn_Convert_Rate',
              sessionProperty: false,
              type: '_id_800019'
            }, {
              property: 'paymentno',
              inpColumn: 'inppaymentno',
              dbColumn: 'Paymentno',
              sessionProperty: false,
              type: '_id_10'
            }, {
              property: 'finaccCurrency',
              inpColumn: 'inpfinaccCurrencyId',
              dbColumn: 'Finacc_Currency_ID',
              sessionProperty: false,
              type: '_id_112'
            }, {
              property: 'canceled',
              inpColumn: 'inpiscanceled',
              dbColumn: 'Iscanceled',
              sessionProperty: false,
              type: '_id_20'
            }, {
              property: 'businessPartner',
              inpColumn: 'inpcBpartnerId',
              dbColumn: 'C_Bpartner_ID',
              sessionProperty: false,
              type: '_id_800057'
            }, {
              property: 'invoiceAmount',
              inpColumn: 'inpinvoicedamt',
              dbColumn: 'Invoicedamt',
              sessionProperty: false,
              type: '_id_12'
            }, {
              property: 'organization',
              inpColumn: 'inpadOrgId',
              dbColumn: 'AD_Org_ID',
              sessionProperty: true,
              type: '_id_30'
            }, {
              property: 'orderPaymentPlan',
              inpColumn: 'inpfinPaymentSchedOrdVId',
              dbColumn: 'Fin_Payment_Sched_Ord_V_Id',
              sessionProperty: false,
              type: '_id_C01DEDDA9B35427786058CB649FB972F'
            }, {
              property: 'active',
              inpColumn: 'inpisactive',
              dbColumn: 'Isactive',
              sessionProperty: false,
              type: '_id_20'
            }, {
              property: 'orderno',
              inpColumn: 'inporderno',
              dbColumn: 'Orderno',
              sessionProperty: false,
              type: '_id_10'
            }, {
              property: 'id',
              inpColumn: 'inpfinPaymentDetailVId',
              dbColumn: 'Fin_Payment_Detail_V_ID',
              sessionProperty: false,
              type: '_id_13'
            }, {
              property: 'invoiceno',
              inpColumn: 'inpinvoiceno',
              dbColumn: 'Invoiceno',
              sessionProperty: false,
              type: '_id_10'
            }, {
              property: 'client',
              inpColumn: 'inpadClientId',
              dbColumn: 'AD_Client_ID',
              sessionProperty: true,
              type: '_id_30'
            }, {
              property: 'id',
              inpColumn: 'Fin_Payment_Detail_V_ID',
              dbColumn: 'Fin_Payment_Detail_V_ID',
              sessionProperty: true,
              type: '_id_13'
            }],
            actionToolbarButtons: [],
            showParentButtons: true,
            buttonsHaveSessionLogic: false,
            iconToolbarButtons: [],
            initWidget: function () {
              this.dataSource = OB.Datasource.create({
                createClassName: 'OBViewDataSource',
                titleField: OB.Constants.IDENTIFIER,
                dataURL: '/openbravo/org.openbravo.service.datasource/FIN_Payment_Detail_V',
                recordXPath: '/response/data',
                dataFormat: 'json',
                operationBindings: [{
                  operationType: 'fetch',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'POST'
                  }
                }, {
                  operationType: 'add',
                  dataProtocol: 'postMessage'
                }, {
                  operationType: 'remove',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'DELETE'
                  }
                }, {
                  operationType: 'update',
                  dataProtocol: 'postMessage',
                  requestProperties: {
                    httpMethod: 'PUT'
                  }
                }],
                requestProperties: {
                  params: {
                    _className: 'OBViewDataSource'
                  }
                },
                fields: [{
                  name: 'id',
                  type: '_id_13',
                  additional: false,
                  hidden: true,
                  primaryKey: true,
                  canSave: false,
                  title: 'id'
                }, {
                  name: 'client',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'client',
                  hidden: true
                }, {
                  name: 'client._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'client'
                }, {
                  name: 'organization',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'organization',
                  hidden: true
                }, {
                  name: 'organization._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'organization'
                }, {
                  name: 'active',
                  type: '_id_20',
                  additional: false,
                  title: 'active'
                }, {
                  name: 'creationDate',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'creationDate'
                }, {
                  name: 'createdBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'createdBy',
                  hidden: true
                }, {
                  name: 'createdBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'createdBy'
                }, {
                  name: 'updated',
                  type: '_id_16',
                  additional: false,
                  canSave: false,
                  title: 'updated'
                }, {
                  name: 'updatedBy',
                  type: '_id_30',
                  additional: false,
                  canSave: false,
                  title: 'updatedBy',
                  hidden: true
                }, {
                  name: 'updatedBy._identifier',
                  type: 'text',
                  hidden: true,
                  canSave: false,
                  title: 'updatedBy'
                }, {
                  name: 'invoicePaymentPlan',
                  type: '_id_89A08501440B470CA3E9E5F399F32D31',
                  additional: false,
                  title: 'invoicePaymentPlan',
                  hidden: true
                }, {
                  name: 'invoicePaymentPlan._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'invoicePaymentPlan'
                }, {
                  name: 'orderPaymentPlan',
                  type: '_id_C01DEDDA9B35427786058CB649FB972F',
                  additional: false,
                  title: 'orderPaymentPlan',
                  hidden: true
                }, {
                  name: 'orderPaymentPlan._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'orderPaymentPlan'
                }, {
                  name: 'invoiceno',
                  type: '_id_10',
                  additional: false,
                  length: 30,
                  title: 'invoiceno'
                }, {
                  name: 'orderno',
                  type: '_id_10',
                  additional: false,
                  length: 30,
                  title: 'orderno'
                }, {
                  name: 'paymentno',
                  type: '_id_10',
                  additional: false,
                  required: true,
                  length: 30,
                  title: 'paymentno'
                }, {
                  name: 'payment',
                  type: '_id_30',
                  additional: false,
                  required: true,
                  title: 'payment',
                  hidden: true
                }, {
                  name: 'payment._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'payment'
                }, {
                  name: 'dueDate',
                  type: '_id_15',
                  additional: false,
                  title: 'dueDate'
                }, {
                  name: 'invoiceAmount',
                  type: '_id_12',
                  additional: false,
                  title: 'invoiceAmount'
                }, {
                  name: 'expected',
                  type: '_id_12',
                  additional: false,
                  title: 'expected'
                }, {
                  name: 'paidAmount',
                  type: '_id_12',
                  additional: false,
                  required: true,
                  title: 'paidAmount'
                }, {
                  name: 'businessPartner',
                  type: '_id_800057',
                  additional: false,
                  required: true,
                  title: 'businessPartner',
                  hidden: true
                }, {
                  name: 'businessPartner._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'businessPartner'
                }, {
                  name: 'paymentMethod',
                  type: '_id_30',
                  additional: false,
                  required: true,
                  title: 'paymentMethod',
                  hidden: true
                }, {
                  name: 'paymentMethod._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'paymentMethod'
                }, {
                  name: 'finFinancialAccount',
                  type: '_id_30',
                  additional: false,
                  required: true,
                  title: 'finFinancialAccount',
                  hidden: true
                }, {
                  name: 'finFinancialAccount._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'finFinancialAccount'
                }, {
                  name: 'currency',
                  type: '_id_30',
                  additional: false,
                  title: 'currency',
                  hidden: true
                }, {
                  name: 'currency._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'currency'
                }, {
                  name: 'paymentDate',
                  type: '_id_15',
                  additional: false,
                  title: 'paymentDate'
                }, {
                  name: 'glitemname',
                  type: '_id_10',
                  additional: false,
                  length: 60,
                  title: 'glitemname'
                }, {
                  name: 'writeoffAmount',
                  type: '_id_12',
                  additional: false,
                  title: 'writeoffAmount'
                }, {
                  name: 'finaccCurrency',
                  type: '_id_112',
                  additional: false,
                  title: 'finaccCurrency',
                  hidden: true
                }, {
                  name: 'finaccCurrency._identifier',
                  type: 'text',
                  hidden: true,
                  title: 'finaccCurrency'
                }, {
                  name: 'finaccTxnConvertRate',
                  type: '_id_800019',
                  additional: false,
                  title: 'finaccTxnConvertRate'
                }, {
                  name: 'paidConverted',
                  type: '_id_12',
                  additional: false,
                  title: 'paidConverted'
                }, {
                  name: 'expectedConverted',
                  type: '_id_12',
                  additional: false,
                  title: 'expectedConverted'
                }, {
                  name: 'canceled',
                  type: '_id_20',
                  additional: false,
                  title: 'canceled'
                }, {
                  name: 'businessPartner.name',
                  type: '_id_10',
                  additional: true,
                  required: true,
                  length: 60,
                  title: 'businessPartner.name'
                }]
              });
              this.viewForm = isc.OBViewForm.create({
                fields: [{
                  name: 'payment',
                  title: 'Payment In',
                  type: '_id_30',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'FIN_Payment_ID',
                  inpColumnName: 'inpfinPaymentId',
                  referencedKeyColumnName: 'Fin_Payment_ID',
                  targetEntity: 'FIN_Payment',
                  required: true,
                  displayField: 'payment._identifier',
                  valueField: 'payment',
                  showPickerIcon: true,
                  width: '*',
                  dummy: ''
                }, {
                  name: 'paymentDate',
                  title: 'Payment In Date',
                  type: '_id_15',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Paymentdate',
                  inpColumnName: 'inppaymentdate',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'dueDate',
                  title: 'Due Date',
                  type: '_id_15',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Duedate',
                  inpColumnName: 'inpduedate',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'paymentMethod',
                  title: 'Payment Method',
                  type: '_id_30',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Fin_Paymentmethod_ID',
                  inpColumnName: 'inpfinPaymentmethodId',
                  referencedKeyColumnName: 'Fin_Paymentmethod_ID',
                  targetEntity: 'FIN_PaymentMethod',
                  required: true,
                  displayField: 'paymentMethod._identifier',
                  valueField: 'paymentMethod',
                  showPickerIcon: true,
                  width: '*',
                  dummy: ''
                }, {
                  name: 'finFinancialAccount',
                  title: 'Financial Account',
                  type: '_id_30',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: true,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Fin_Financial_Account_ID',
                  inpColumnName: 'inpfinFinancialAccountId',
                  referencedKeyColumnName: 'Fin_Financial_Account_ID',
                  targetEntity: 'FIN_Financial_Account',
                  required: true,
                  displayField: 'finFinancialAccount._identifier',
                  valueField: 'finFinancialAccount',
                  showPickerIcon: true,
                  width: '*',
                  dummy: ''
                }, {
                  name: 'expected',
                  title: 'Expected Amount',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Expected',
                  inpColumnName: 'inpexpected',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'paidAmount',
                  title: 'Received Amount',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Paidamt',
                  inpColumnName: 'inppaidamt',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: true,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'writeoffAmount',
                  title: 'Write-off Amount',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Writeoffamt',
                  inpColumnName: 'inpwriteoffamt',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'expectedConverted',
                  title: 'Expected (Account Currency)',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'ExpectedConverted',
                  inpColumnName: 'inpexpectedconverted',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  showIf: function (item, value, form, values) {
                    var context = form.view.getContextInfo(false, true, true),
                        currentValues = values || form.view.getCurrentValues();
                    OB.Utilities.fixNull250(currentValues);
                    return !this.hiddenInForm && context && (currentValues.currency !== currentValues.finaccCurrency);
                  },
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'paidConverted',
                  title: 'Received (Account Currency)',
                  type: '_id_12',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'PaidConverted',
                  inpColumnName: 'inppaidconverted',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  showIf: function (item, value, form, values) {
                    var context = form.view.getContextInfo(false, true, true),
                        currentValues = values || form.view.getCurrentValues();
                    OB.Utilities.fixNull250(currentValues);
                    return !this.hiddenInForm && context && (currentValues.currency !== currentValues.finaccCurrency);
                  },
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'finaccTxnConvertRate',
                  title: 'Exchange Rate',
                  type: '_id_800019',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Finacc_Txn_Convert_Rate',
                  inpColumnName: 'inpfinaccTxnConvertRate',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  showIf: function (item, value, form, values) {
                    var context = form.view.getContextInfo(false, true, true),
                        currentValues = values || form.view.getCurrentValues();
                    OB.Utilities.fixNull250(currentValues);
                    return !this.hiddenInForm && context && (currentValues.currency !== currentValues.finaccCurrency);
                  },
                  "width": "50%",
                  dummy: ''
                }, {
                  name: 'canceled',
                  title: 'Canceled',
                  type: '_id_20',
                  disabled: true,
                  readonly: true,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: true,
                  hasDefaultValue: false,
                  columnName: 'Iscanceled',
                  inpColumnName: 'inpiscanceled',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  "width": 1,
                  "overflow": "visible",
                  dummy: ''
                }, {
                  name: '1000100001',
                  title: 'Audit',
                  type: 'OBAuditSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'Audit',
                  itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
                  dummy: ''
                }, {
                  name: 'creationDate',
                  title: 'Creation Date',
                  type: '_id_16',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'createdBy',
                  title: 'Created By',
                  type: '_id_30',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: 'User',
                  required: false,
                  displayField: 'createdBy._identifier',
                  valueField: 'createdBy',
                  showPickerIcon: true,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'updated',
                  title: 'Updated',
                  type: '_id_16',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: '',
                  required: false,
                  'width': '*',
                  dummy: ''
                }, {
                  name: 'updatedBy',
                  title: 'Updated By',
                  type: '_id_30',
                  disabled: true,
                  readonly: true,
                  updatable: false,
                  parentProperty: false,
                  colSpan: 1,
                  rowSpan: 1,
                  startRow: false,
                  endRow: false,
                  personalizable: false,
                  hasDefaultValue: false,
                  columnName: '',
                  inpColumnName: '',
                  referencedKeyColumnName: '',
                  targetEntity: 'User',
                  required: false,
                  displayField: 'updatedBy._identifier',
                  valueField: 'updatedBy',
                  showPickerIcon: true,
                  'width': '*',
                  dummy: ''
                }, {
                  name: '_notes_',
                  title: 'dummy',
                  type: 'OBNoteSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_notes_Canvas'],
                  dummy: ''
                }, {
                  name: '_notes_Canvas',
                  title: 'dummy',
                  type: 'OBNoteCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }, {
                  name: '_linkedItems_',
                  title: 'dummy',
                  type: 'OBLinkedItemSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_linkedItems_Canvas'],
                  dummy: ''
                }, {
                  name: '_linkedItems_Canvas',
                  title: 'dummy',
                  type: 'OBLinkedItemCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }, {
                  name: '_attachments_',
                  title: 'dummy',
                  type: 'OBAttachmentsSectionItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  sectionExpanded: false,
                  defaultValue: 'dummy',
                  itemIds: ['_attachments_Canvas'],
                  dummy: ''
                }, {
                  name: '_attachments_Canvas',
                  title: '',
                  type: 'OBAttachmentCanvasItem',
                  disabled: false,
                  readonly: false,
                  updatable: true,
                  parentProperty: false,
                  colSpan: 4,
                  rowSpan: 1,
                  startRow: true,
                  endRow: true,
                  personalizable: false,
                  hasDefaultValue: false,
                  dummy: ''
                }],
                statusBarFields: [],
                obFormProperties: {
                  onFieldChanged: function (form, item, value) {
                    var f = form || this,
                        context = this.view.getContextInfo(false, true),
                        currentValues = f.view.getCurrentValues(),
                        otherItem;
                  }
                }
              });
              this.viewGrid = isc.OBViewGrid.create({
                uiPattern: 'RO',
                fields: [{
                  autoExpand: false,
                  type: '_id_15',
                  cellAlign: 'left',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Paymentdate',
                    inpColumnName: 'inppaymentdate',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'paymentDate',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterOnKeypress: false,
                  canFilter: true,
                  filterEditorType: 'OBMiniDateRangeItem',
                  title: 'Payment In Date',
                  prompt: 'Payment In Date',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Paymentdate',
                  inpColumnName: 'inppaymentdate',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_15',
                  cellAlign: 'left',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Duedate',
                    inpColumnName: 'inpduedate',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'dueDate',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterOnKeypress: false,
                  canFilter: true,
                  filterEditorType: 'OBMiniDateRangeItem',
                  title: 'Due Date',
                  prompt: 'Due Date',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Duedate',
                  inpColumnName: 'inpduedate',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_10',
                  editorProperties: {
                    width: '',
                    columnName: 'Paymentno',
                    inpColumnName: 'inppaymentno',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'paymentno')])",
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'paymentno',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBTextItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem',
                  title: 'Payment In No.',
                  prompt: 'Payment In No.',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Paymentno',
                  inpColumnName: 'inppaymentno',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'Fin_Paymentmethod_ID',
                    inpColumnName: 'inpfinPaymentmethodId',
                    referencedKeyColumnName: 'Fin_Paymentmethod_ID',
                    targetEntity: 'FIN_PaymentMethod',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'paymentMethod._identifier',
                  valueField: 'paymentMethod',
                  foreignKeyField: true,
                  name: 'paymentMethod',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'paymentMethod')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Payment Method',
                  prompt: 'Payment Method',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Fin_Paymentmethod_ID',
                  inpColumnName: 'inpfinPaymentmethodId',
                  referencedKeyColumnName: 'Fin_Paymentmethod_ID',
                  targetEntity: 'FIN_PaymentMethod'
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Expected',
                    inpColumnName: 'inpexpected',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'expected',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Expected Amount',
                  prompt: 'Expected Amount',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Expected',
                  inpColumnName: 'inpexpected',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Paidamt',
                    inpColumnName: 'inppaidamt',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'paidAmount',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Received Amount',
                  prompt: 'Received Amount',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Paidamt',
                  inpColumnName: 'inppaidamt',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Writeoffamt',
                    inpColumnName: 'inpwriteoffamt',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'writeoffAmount',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Write-off Amount',
                  prompt: 'Write-off Amount',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Writeoffamt',
                  inpColumnName: 'inpwriteoffamt',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'Fin_Financial_Account_ID',
                    inpColumnName: 'inpfinFinancialAccountId',
                    referencedKeyColumnName: 'Fin_Financial_Account_ID',
                    targetEntity: 'FIN_Financial_Account',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'finFinancialAccount._identifier',
                  valueField: 'finFinancialAccount',
                  foreignKeyField: true,
                  name: 'finFinancialAccount',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'finFinancialAccount')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Financial Account',
                  prompt: 'Financial Account',
                  required: true,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Fin_Financial_Account_ID',
                  inpColumnName: 'inpfinFinancialAccountId',
                  referencedKeyColumnName: 'Fin_Financial_Account_ID',
                  targetEntity: 'FIN_Financial_Account'
                }, {
                  autoExpand: false,
                  type: '_id_20',
                  editorProperties: {
                    "width": 1,
                    "overflow": "visible",
                    "showTitle": false,
                    "showLabel": false,
                    columnName: 'Iscanceled',
                    inpColumnName: 'inpiscanceled',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: true,
                    readonly: true,
                    updatable: true
                  },
                  name: 'canceled',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBCheckboxItem',
                  width: '*',
                  autoFitWidth: false,
                  formatCellValue: function (value, record, rowNum, colNum, grid) {
                    return OB.Utilities.getYesNoDisplayValue(value);
                  },
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem',
                  title: 'Canceled',
                  prompt: 'Canceled',
                  required: false,
                  escapeHTML: true,
                  showIf: 'true',
                  columnName: 'Iscanceled',
                  inpColumnName: 'inpiscanceled',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'FIN_Payment_ID',
                    inpColumnName: 'inpfinPaymentId',
                    referencedKeyColumnName: 'Fin_Payment_ID',
                    targetEntity: 'FIN_Payment',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'payment._identifier',
                  valueField: 'payment',
                  foreignKeyField: true,
                  name: 'payment',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'payment')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Payment In',
                  prompt: 'Payment In',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'FIN_Payment_ID',
                  inpColumnName: 'inpfinPaymentId',
                  referencedKeyColumnName: 'Fin_Payment_ID',
                  targetEntity: 'FIN_Payment'
                }, {
                  autoExpand: true,
                  type: '_id_30',
                  editorProperties: {
                    width: '',
                    columnName: 'C_Currency_ID',
                    inpColumnName: 'inpcCurrencyId',
                    referencedKeyColumnName: 'C_Currency_ID',
                    targetEntity: 'Currency',
                    disabled: false,
                    readonly: false,
                    updatable: true,
                    redrawOnChange: true,
                    changed: function (form, item, value) {
                      if (this.pickValue && !this._pickedValue) {
                        return;
                      }
                      this.Super('changed', arguments);
                      form.onFieldChanged(form, item, value);
                      form.view.toolBar.refreshCustomButtonsView(form.view);
                    }
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'currency._identifier',
                  valueField: 'currency',
                  foreignKeyField: true,
                  name: 'currency',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'currency')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Currency',
                  prompt: 'Currency',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'C_Currency_ID',
                  inpColumnName: 'inpcCurrencyId',
                  referencedKeyColumnName: 'C_Currency_ID',
                  targetEntity: 'Currency'
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'ExpectedConverted',
                    inpColumnName: 'inpexpectedconverted',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true,
                    showIf: function (item, value, form, currentValues) {
                      currentValues = currentValues || form.view.getCurrentValues();
                      var context = form.view.getContextInfo(false, true);
                      return context && (currentValues.currency !== currentValues.finaccCurrency);
                    }
                  },
                  name: 'expectedConverted',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Expected (Account Currency)',
                  prompt: 'Expected (Account Currency)',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'ExpectedConverted',
                  inpColumnName: 'inpexpectedconverted',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'PaidConverted',
                    inpColumnName: 'inppaidconverted',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true,
                    showIf: function (item, value, form, currentValues) {
                      currentValues = currentValues || form.view.getCurrentValues();
                      var context = form.view.getContextInfo(false, true);
                      return context && (currentValues.currency !== currentValues.finaccCurrency);
                    }
                  },
                  name: 'paidConverted',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Received (Account Currency)',
                  prompt: 'Received (Account Currency)',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'PaidConverted',
                  inpColumnName: 'inppaidconverted',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_800019',
                  editorProperties: {
                    "width": "50%",
                    columnName: 'Finacc_Txn_Convert_Rate',
                    inpColumnName: 'inpfinaccTxnConvertRate',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true,
                    showIf: function (item, value, form, currentValues) {
                      currentValues = currentValues || form.view.getCurrentValues();
                      var context = form.view.getContextInfo(false, true);
                      return context && (currentValues.currency !== currentValues.finaccCurrency);
                    }
                  },
                  name: 'finaccTxnConvertRate',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Exchange Rate',
                  prompt: 'Exchange Rate',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Finacc_Txn_Convert_Rate',
                  inpColumnName: 'inpfinaccTxnConvertRate',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_112',
                  editorProperties: {
                    displayField: null,
                    valueField: null,
                    columnName: 'Finacc_Currency_ID',
                    inpColumnName: 'inpfinaccCurrencyId',
                    referencedKeyColumnName: 'C_Currency_ID',
                    targetEntity: 'Currency',
                    disabled: false,
                    readonly: false,
                    updatable: true,
                    redrawOnChange: true,
                    changed: function (form, item, value) {
                      if (this.pickValue && !this._pickedValue) {
                        return;
                      }
                      this.Super('changed', arguments);
                      form.onFieldChanged(form, item, value);
                      form.view.toolBar.refreshCustomButtonsView(form.view);
                    }
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'finaccCurrency._identifier',
                  valueField: 'finaccCurrency',
                  foreignKeyField: true,
                  name: 'finaccCurrency',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBFKItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'finaccCurrency')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Finacc_Currency_ID',
                  prompt: 'Finacc_Currency_ID',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Finacc_Currency_ID',
                  inpColumnName: 'inpfinaccCurrencyId',
                  referencedKeyColumnName: 'C_Currency_ID',
                  targetEntity: 'Currency'
                }, {
                  autoExpand: true,
                  type: '_id_30',
                  editorProperties: {
                    width: '',
                    columnName: 'AD_Client_ID',
                    inpColumnName: 'inpadClientId',
                    referencedKeyColumnName: 'AD_Client_ID',
                    targetEntity: 'ADClient',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'client._identifier',
                  valueField: 'client',
                  foreignKeyField: true,
                  name: 'client',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Client',
                  prompt: 'Client',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'AD_Client_ID',
                  inpColumnName: 'inpadClientId',
                  referencedKeyColumnName: 'AD_Client_ID',
                  targetEntity: 'ADClient'
                }, {
                  autoExpand: true,
                  type: '_id_800057',
                  editorProperties: {
                    selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
                    popupTextMatchStyle: 'startsWith',
                    textMatchStyle: 'startsWith',
                    defaultPopupFilterField: 'name',
                    displayField: 'name',
                    valueField: 'bpid',
                    pickListFields: [{
                      title: ' ',
                      name: 'name',
                      disableFilter: true,
                      canSort: false,
                      type: 'text'
                    }, {
                      title: 'Location',
                      name: 'locationname',
                      disableFilter: true,
                      canSort: false,
                      type: '_id_10'
                    }, {
                      title: 'Contact',
                      name: 'contactname',
                      disableFilter: true,
                      canSort: false,
                      type: '_id_10'
                    }],
                    showSelectorGrid: true,
                    selectorGridFields: [{
                      title: 'Name',
                      name: 'name',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_10',
                      filterOnKeypress: true,
                      canFilter: true,
                      filterEditorType: 'OBTextFilterItem'
                    }, {
                      title: 'Value',
                      name: 'value',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_10',
                      filterOnKeypress: true,
                      canFilter: true,
                      filterEditorType: 'OBTextFilterItem'
                    }, {
                      title: 'Credit Line available',
                      name: 'creditAvailable',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_12',
                      canFilter: true,
                      filterEditorType: 'OBNumberFilterItem'
                    }, {
                      title: 'Customer Balance',
                      name: 'creditUsed',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_12',
                      canFilter: true,
                      filterEditorType: 'OBNumberFilterItem'
                    }, {
                      title: 'Location',
                      name: 'locationname',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_10',
                      filterOnKeypress: true,
                      canFilter: true,
                      filterEditorType: 'OBTextFilterItem'
                    }, {
                      title: 'Contact',
                      name: 'contactname',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_10',
                      filterOnKeypress: true,
                      canFilter: true,
                      filterEditorType: 'OBTextFilterItem'
                    }, {
                      title: 'Customer',
                      name: 'customer',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_20',
                      filterOnKeypress: true,
                      canFilter: true,
                      filterEditorType: 'OBYesNoItem'
                    }, {
                      title: 'Vendor',
                      name: 'vendor',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_20',
                      filterOnKeypress: true,
                      canFilter: true,
                      filterEditorType: 'OBYesNoItem'
                    }, {
                      title: 'Income',
                      name: 'income',
                      disableFilter: false,
                      canSort: true,
                      type: '_id_12',
                      canFilter: true,
                      filterEditorType: 'OBNumberFilterItem'
                    }],
                    outFields: {
                      'id': {
                        'fieldName': '',
                        'suffix': ''
                      },
                      '_identifier': {
                        'fieldName': '',
                        'suffix': ''
                      },
                      'locationid': {
                        'fieldName': 'locationid',
                        'suffix': '_LOC'
                      },
                      'contactid': {
                        'fieldName': 'contactid',
                        'suffix': '_CON'
                      }
                    },
                    extraSearchFields: ['value'],
                    optionDataSource: OB.Datasource.create({
                      createClassName: '',
                      titleField: OB.Constants.IDENTIFIER,
                      dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
                      recordXPath: '/response/data',
                      dataFormat: 'json',
                      operationBindings: [{
                        operationType: 'fetch',
                        dataProtocol: 'postParams',
                        requestProperties: {
                          httpMethod: 'POST'
                        }
                      }, {
                        operationType: 'add',
                        dataProtocol: 'postMessage'
                      }, {
                        operationType: 'remove',
                        dataProtocol: 'postParams',
                        requestProperties: {
                          httpMethod: 'DELETE'
                        }
                      }, {
                        operationType: 'update',
                        dataProtocol: 'postMessage',
                        requestProperties: {
                          httpMethod: 'PUT'
                        }
                      }],
                      requestProperties: {
                        params: {
                          targetProperty: 'businessPartner',
                          adTabId: 'B82C02920AA84E8DB57D553185BD2F06',
                          IsSelectorItem: 'true',
                          columnName: 'C_Bpartner_ID',
                          _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
                        }
                      },
                      fields: []
                    }),
                    whereClause: '',
                    outHiddenInputPrefix: 'inpcBpartnerId',
                    width: '',
                    columnName: 'C_Bpartner_ID',
                    inpColumnName: 'inpcBpartnerId',
                    referencedKeyColumnName: 'C_BPartner_ID',
                    targetEntity: 'BusinessPartner',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'businessPartner._identifier',
                  valueField: 'businessPartner',
                  foreignKeyField: true,
                  name: 'businessPartner',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSelectorItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'businessPartner')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Business Partner',
                  prompt: 'Business Partner',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'C_Bpartner_ID',
                  inpColumnName: 'inpcBpartnerId',
                  referencedKeyColumnName: 'C_BPartner_ID',
                  targetEntity: 'BusinessPartner'
                }, {
                  autoExpand: false,
                  type: '_id_12',
                  editorProperties: {
                    "width": "",
                    columnName: 'Invoicedamt',
                    inpColumnName: 'inpinvoicedamt',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'invoiceAmount',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBNumberItem',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem',
                  title: 'Invoice Amount',
                  prompt: 'Invoice Amount',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Invoicedamt',
                  inpColumnName: 'inpinvoicedamt',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_30',
                  editorProperties: {
                    width: '',
                    columnName: 'AD_Org_ID',
                    inpColumnName: 'inpadOrgId',
                    referencedKeyColumnName: 'AD_Org_ID',
                    targetEntity: 'Organization',
                    disabled: false,
                    readonly: false,
                    updatable: false
                  },
                  width: isc.OBGrid.getDefaultColumnWidth(32),
                  displayField: 'organization._identifier',
                  valueField: 'organization',
                  foreignKeyField: true,
                  name: 'organization',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBFKFilterTextItem',
                  title: 'Organization',
                  prompt: 'Organization',
                  required: true,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'AD_Org_ID',
                  inpColumnName: 'inpadOrgId',
                  referencedKeyColumnName: 'AD_Org_ID',
                  targetEntity: 'Organization'
                }, {
                  autoExpand: false,
                  type: '_id_20',
                  editorProperties: {
                    "width": 1,
                    "overflow": "visible",
                    "showTitle": false,
                    "showLabel": false,
                    columnName: 'Isactive',
                    inpColumnName: 'inpisactive',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  name: 'active',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBCheckboxItem',
                  width: '*',
                  autoFitWidth: false,
                  formatCellValue: function (value, record, rowNum, colNum, grid) {
                    return OB.Utilities.getYesNoDisplayValue(value);
                  },
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem',
                  title: 'Active',
                  prompt: 'Active',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Isactive',
                  inpColumnName: 'inpisactive',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_10',
                  editorProperties: {
                    width: '',
                    columnName: 'Orderno',
                    inpColumnName: 'inporderno',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'orderno')])",
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'orderno',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBTextItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem',
                  title: 'Order No.',
                  prompt: 'Order No.',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Orderno',
                  inpColumnName: 'inporderno',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: true,
                  type: '_id_10',
                  editorProperties: {
                    width: '',
                    columnName: 'Invoiceno',
                    inpColumnName: 'inpinvoiceno',
                    referencedKeyColumnName: '',
                    targetEntity: '',
                    disabled: false,
                    readonly: false,
                    updatable: true
                  },
                  showHover: true,
                  hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'invoiceno')])",
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'invoiceno',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBTextItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem',
                  title: 'Invoice No.',
                  prompt: 'Invoice No.',
                  required: false,
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'Invoiceno',
                  inpColumnName: 'inpinvoiceno',
                  referencedKeyColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_16',
                  editorProperties: {
                    width: '*',
                    columnName: 'creationDate',
                    targetEntity: '',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'creationDate',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterEditorType: 'OBMiniDateRangeItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Creation Date',
                  prompt: 'Creation Date',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'creationDate',
                  inpColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'createdBy',
                    targetEntity: 'User',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'createdBy',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  filterEditorType: 'OBFKFilterTextItem',
                  displayField: 'createdBy._identifier',
                  valueField: 'createdBy',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Created By',
                  prompt: 'Created By',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'createdBy',
                  inpColumnName: '',
                  targetEntity: 'User'
                }, {
                  autoExpand: false,
                  type: '_id_16',
                  editorProperties: {
                    width: '*',
                    columnName: 'updated',
                    targetEntity: '',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'updated',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBDateItem',
                  filterEditorType: 'OBMiniDateRangeItem',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Updated',
                  prompt: 'Updated',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'updated',
                  inpColumnName: '',
                  targetEntity: ''
                }, {
                  autoExpand: false,
                  type: '_id_30',
                  editorProperties: {
                    width: '*',
                    columnName: 'updatedBy',
                    targetEntity: 'User',
                    disabled: true,
                    updatable: false
                  },
                  showHover: false,
                  width: isc.OBGrid.getDefaultColumnWidth(30),
                  name: 'updatedBy',
                  canExport: true,
                  canHide: true,
                  editorType: 'OBSearchItem',
                  filterEditorType: 'OBFKFilterTextItem',
                  displayField: 'updatedBy._identifier',
                  valueField: 'updatedBy',
                  filterOnKeypress: true,
                  canFilter: true,
                  required: false,
                  title: 'Updated By',
                  prompt: 'Updated By',
                  escapeHTML: true,
                  showIf: 'false',
                  columnName: 'updatedBy',
                  inpColumnName: '',
                  targetEntity: 'User'
                }],
                autoExpandFieldNames: ['paymentMethod', 'finFinancialAccount', 'payment', 'currency', 'client', 'businessPartner', 'organization', 'paymentno', 'orderno', 'invoiceno', 'finaccCurrency'],
                whereClause: '',
                orderByClause: '',
                sortField: 'paymentDate',
                filterClause: '',
                filterName: '',
                foreignKeyFieldNames: ['paymentMethod', 'finFinancialAccount', 'payment', 'currency', 'finaccCurrency', 'client', 'businessPartner', 'organization']
              });
              this.Super('initWidget', arguments);
            },
            createViewStructure: function () {}
          }));
        }
      }));
      this.addChildView(isc.OBStandardView.create({
        tabTitle: 'Payment',
        entity: 'FinancialMgmtDebtPayment',
        parentProperty: 'salesOrder',
        tabId: '800219',
        moduleId: '0',
        defaultEditMode: false,
        mapping250: '/SalesOrder/Payment',
        isAcctTab: false,
        isTrlTab: false,
        standardProperties: {
          inpTabId: '800219',
          inpwindowId: '143',
          inpTableId: '800018',
          inpkeyColumnId: 'C_Debt_Payment_ID',
          inpKeyName: 'inpcDebtPaymentId'
        },
        propertyToColumns: [{
          property: 'settlementCancelled',
          inpColumn: 'inpcSettlementCancelId',
          dbColumn: 'C_Settlement_Cancel_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'formOfPayment',
          inpColumn: 'inppaymentrule',
          dbColumn: 'PaymentRule',
          sessionProperty: false,
          type: '_id_195'
        }, {
          property: 'dueDate',
          inpColumn: 'inpdateplanned',
          dbColumn: 'Dateplanned',
          sessionProperty: false,
          type: '_id_15'
        }, {
          property: 'businessPartner',
          inpColumn: 'inpcBpartnerId',
          dbColumn: 'C_BPartner_ID',
          sessionProperty: false,
          type: '_id_800057'
        }, {
          property: 'description',
          inpColumn: 'inpdescription',
          dbColumn: 'Description',
          sessionProperty: false,
          type: '_id_14'
        }, {
          property: 'cashbook',
          inpColumn: 'inpcCashbookId',
          dbColumn: 'C_CashBook_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'cashJournalLine',
          inpColumn: 'inpcCashlineId',
          dbColumn: 'C_CashLine_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'bankAccount',
          inpColumn: 'inpcBankaccountId',
          dbColumn: 'C_BankAccount_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'bankStatementLine',
          inpColumn: 'inpcBankstatementlineId',
          dbColumn: 'C_BankStatementLine_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'amount',
          inpColumn: 'inpamount',
          dbColumn: 'Amount',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'currency',
          inpColumn: 'inpcCurrencyId',
          dbColumn: 'C_Currency_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'writeoffAmount',
          inpColumn: 'inpwriteoffamt',
          dbColumn: 'WriteOffAmt',
          sessionProperty: false,
          type: '_id_12'
        }, {
          property: 'initialStatus',
          inpColumn: 'inpstatusInitial',
          dbColumn: 'Status_Initial',
          sessionProperty: false,
          type: '_id_800070'
        }, {
          property: 'project',
          inpColumn: 'inpcProjectId',
          dbColumn: 'C_Project_ID',
          sessionProperty: false,
          type: '_id_19'
        }, {
          property: 'receipt',
          inpColumn: 'inpisreceipt',
          dbColumn: 'IsReceipt',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'paymentComplete',
          inpColumn: 'inpispaid',
          dbColumn: 'IsPaid',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'organization',
          inpColumn: 'inpadOrgId',
          dbColumn: 'AD_Org_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'valid',
          inpColumn: 'inpisvalid',
          dbColumn: 'IsValid',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'changeSettlementCancel',
          inpColumn: 'inpchangesettlementcancel',
          dbColumn: 'Changesettlementcancel',
          sessionProperty: false,
          type: '_id_28'
        }, {
          property: 'cancelProcessed',
          inpColumn: 'inpcancelProcessed',
          dbColumn: 'Cancel_Processed',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'manual',
          inpColumn: 'inpismanual',
          dbColumn: 'IsManual',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'id',
          inpColumn: 'inpcDebtPaymentId',
          dbColumn: 'C_Debt_Payment_ID',
          sessionProperty: false,
          type: '_id_13'
        }, {
          property: 'generateProcessed',
          inpColumn: 'inpgenerateProcessed',
          dbColumn: 'Generate_Processed',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'settlementGenerate',
          inpColumn: 'inpcSettlementGenerateId',
          dbColumn: 'C_Settlement_Generate_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'invoice',
          inpColumn: 'inpcInvoiceId',
          dbColumn: 'C_Invoice_ID',
          sessionProperty: false,
          type: '_id_800059'
        }, {
          property: 'active',
          inpColumn: 'inpisactive',
          dbColumn: 'IsActive',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'isAutomaticGenerated',
          inpColumn: 'inpisautomaticgenerated',
          dbColumn: 'IsAutomaticGenerated',
          sessionProperty: false,
          type: '_id_20'
        }, {
          property: 'salesOrder',
          inpColumn: 'inpcOrderId',
          dbColumn: 'C_Order_ID',
          sessionProperty: false,
          type: '_id_30'
        }, {
          property: 'client',
          inpColumn: 'inpadClientId',
          dbColumn: 'AD_Client_ID',
          sessionProperty: true,
          type: '_id_19'
        }, {
          property: 'id',
          inpColumn: 'C_Debt_Payment_ID',
          dbColumn: 'C_Debt_Payment_ID',
          sessionProperty: true,
          type: '_id_13'
        }],
        actionToolbarButtons: [],
        showParentButtons: true,
        buttonsHaveSessionLogic: false,
        iconToolbarButtons: [],
        initWidget: function () {
          this.dataSource = OB.Datasource.create({
            createClassName: 'OBViewDataSource',
            titleField: OB.Constants.IDENTIFIER,
            dataURL: '/openbravo/org.openbravo.service.datasource/FinancialMgmtDebtPayment',
            recordXPath: '/response/data',
            dataFormat: 'json',
            operationBindings: [{
              operationType: 'fetch',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'POST'
              }
            }, {
              operationType: 'add',
              dataProtocol: 'postMessage'
            }, {
              operationType: 'remove',
              dataProtocol: 'postParams',
              requestProperties: {
                httpMethod: 'DELETE'
              }
            }, {
              operationType: 'update',
              dataProtocol: 'postMessage',
              requestProperties: {
                httpMethod: 'PUT'
              }
            }],
            requestProperties: {
              params: {
                _className: 'OBViewDataSource'
              }
            },
            fields: [{
              name: 'id',
              type: '_id_13',
              additional: false,
              hidden: true,
              primaryKey: true,
              required: true,
              title: 'id'
            }, {
              name: 'client',
              type: '_id_19',
              additional: false,
              canSave: false,
              title: 'client',
              hidden: true
            }, {
              name: 'client._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'client'
            }, {
              name: 'organization',
              type: '_id_19',
              additional: false,
              required: true,
              title: 'organization',
              hidden: true
            }, {
              name: 'organization._identifier',
              type: 'text',
              hidden: true,
              title: 'organization'
            }, {
              name: 'active',
              type: '_id_20',
              additional: false,
              title: 'active'
            }, {
              name: 'creationDate',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'creationDate'
            }, {
              name: 'createdBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'createdBy',
              hidden: true
            }, {
              name: 'createdBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'createdBy'
            }, {
              name: 'updated',
              type: '_id_16',
              additional: false,
              canSave: false,
              title: 'updated'
            }, {
              name: 'updatedBy',
              type: '_id_30',
              additional: false,
              canSave: false,
              title: 'updatedBy',
              hidden: true
            }, {
              name: 'updatedBy._identifier',
              type: 'text',
              hidden: true,
              canSave: false,
              title: 'updatedBy'
            }, {
              name: 'receipt',
              type: '_id_20',
              additional: false,
              title: 'receipt'
            }, {
              name: 'settlementCancelled',
              type: '_id_30',
              additional: false,
              title: 'settlementCancelled',
              hidden: true
            }, {
              name: 'settlementCancelled._identifier',
              type: 'text',
              hidden: true,
              title: 'settlementCancelled'
            }, {
              name: 'settlementGenerate',
              type: '_id_30',
              additional: false,
              title: 'settlementGenerate',
              hidden: true
            }, {
              name: 'settlementGenerate._identifier',
              type: 'text',
              hidden: true,
              title: 'settlementGenerate'
            }, {
              name: 'description',
              type: '_id_14',
              additional: false,
              length: 255,
              title: 'description'
            }, {
              name: 'invoice',
              type: '_id_800059',
              additional: false,
              title: 'invoice',
              hidden: true
            }, {
              name: 'invoice._identifier',
              type: 'text',
              hidden: true,
              title: 'invoice'
            }, {
              name: 'businessPartner',
              type: '_id_800057',
              additional: false,
              title: 'businessPartner',
              hidden: true
            }, {
              name: 'businessPartner._identifier',
              type: 'text',
              hidden: true,
              title: 'businessPartner'
            }, {
              name: 'currency',
              type: '_id_19',
              additional: false,
              required: true,
              title: 'currency',
              hidden: true
            }, {
              name: 'currency._identifier',
              type: 'text',
              hidden: true,
              title: 'currency'
            }, {
              name: 'cashJournalLine',
              type: '_id_30',
              additional: false,
              title: 'cashJournalLine',
              hidden: true
            }, {
              name: 'cashJournalLine._identifier',
              type: 'text',
              hidden: true,
              title: 'cashJournalLine'
            }, {
              name: 'bankAccount',
              type: '_id_19',
              additional: false,
              title: 'bankAccount',
              hidden: true
            }, {
              name: 'bankAccount._identifier',
              type: 'text',
              hidden: true,
              title: 'bankAccount'
            }, {
              name: 'cashbook',
              type: '_id_19',
              additional: false,
              title: 'cashbook',
              hidden: true
            }, {
              name: 'cashbook._identifier',
              type: 'text',
              hidden: true,
              title: 'cashbook'
            }, {
              name: 'formOfPayment',
              type: '_id_195',
              additional: false,
              required: true,
              length: 60,
              title: 'formOfPayment',
              valueMap: {
                '5': 'Bank Deposit',
                'R': 'Bank Remittance',
                'B': 'Cash',
                'C': 'Cash on Delivery',
                '2': 'Check',
                'K': 'Credit Card',
                '4': 'Money Order',
                'P': 'On Credit',
                '3': 'Promissory Note',
                '1': 'Wire Transfer',
                'W': 'Withholding'
              }
            }, {
              name: 'paymentComplete',
              type: '_id_20',
              additional: false,
              title: 'paymentComplete'
            }, {
              name: 'amount',
              type: '_id_12',
              additional: false,
              required: true,
              title: 'amount'
            }, {
              name: 'writeoffAmount',
              type: '_id_12',
              additional: false,
              title: 'writeoffAmount'
            }, {
              name: 'dueDate',
              type: '_id_15',
              additional: false,
              required: true,
              title: 'dueDate'
            }, {
              name: 'manual',
              type: '_id_20',
              additional: false,
              title: 'manual'
            }, {
              name: 'valid',
              type: '_id_20',
              additional: false,
              title: 'valid'
            }, {
              name: 'bankStatementLine',
              type: '_id_30',
              additional: false,
              title: 'bankStatementLine',
              hidden: true
            }, {
              name: 'bankStatementLine._identifier',
              type: 'text',
              hidden: true,
              title: 'bankStatementLine'
            }, {
              name: 'changeSettlementCancel',
              type: '_id_28',
              additional: false,
              title: 'changeSettlementCancel'
            }, {
              name: 'cancelProcessed',
              type: '_id_20',
              additional: false,
              title: 'cancelProcessed'
            }, {
              name: 'generateProcessed',
              type: '_id_20',
              additional: false,
              title: 'generateProcessed'
            }, {
              name: 'balancingAmount',
              type: '_id_12',
              additional: false,
              title: 'balancingAmount'
            }, {
              name: 'directPosting',
              type: '_id_20',
              additional: false,
              title: 'directPosting'
            }, {
              name: 'gLItem',
              type: '_id_19',
              additional: false,
              title: 'gLItem',
              hidden: true
            }, {
              name: 'gLItem._identifier',
              type: 'text',
              hidden: true,
              title: 'gLItem'
            }, {
              name: 'salesOrder',
              type: '_id_30',
              additional: false,
              title: 'salesOrder',
              hidden: true
            }, {
              name: 'salesOrder._identifier',
              type: 'text',
              hidden: true,
              title: 'salesOrder'
            }, {
              name: 'project',
              type: '_id_19',
              additional: false,
              title: 'project',
              hidden: true
            }, {
              name: 'project._identifier',
              type: 'text',
              hidden: true,
              title: 'project'
            }, {
              name: 'isAutomaticGenerated',
              type: '_id_20',
              additional: false,
              title: 'isAutomaticGenerated'
            }, {
              name: 'status',
              type: '_id_800070',
              additional: false,
              canSave: false,
              length: 60,
              title: 'status',
              valueMap: {
                'DE': '--',
                'AN': 'Advance',
                'RT': 'In Remittance',
                'RC': 'Received',
                'RE': 'Returned',
                'SE': 'Sent'
              }
            }, {
              name: 'initialStatus',
              type: '_id_800070',
              additional: false,
              canSave: false,
              length: 60,
              title: 'initialStatus',
              valueMap: {
                'DE': '--',
                'AN': 'Advance',
                'RT': 'In Remittance',
                'RC': 'Received',
                'RE': 'Returned',
                'SE': 'Sent'
              }
            }, {
              name: 'withholding',
              type: '_id_19',
              additional: false,
              title: 'withholding',
              hidden: true
            }, {
              name: 'withholding._identifier',
              type: 'text',
              hidden: true,
              title: 'withholding'
            }, {
              name: 'withholdingamount',
              type: '_id_12',
              additional: false,
              title: 'withholdingamount'
            }, {
              name: 'businessPartner.name',
              type: '_id_10',
              additional: true,
              required: true,
              length: 60,
              title: 'businessPartner.name'
            }]
          });
          this.viewForm = isc.OBViewForm.create({
            fields: [{
              name: 'settlementCancelled',
              title: 'Settlement Cancelled',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Settlement_Cancel_ID',
              inpColumnName: 'inpcSettlementCancelId',
              referencedKeyColumnName: 'C_Settlement_ID',
              targetEntity: 'FinancialMgmtSettlement',
              required: false,
              displayField: 'settlementCancelled._identifier',
              valueField: 'settlementCancelled',
              showPickerIcon: true,
              width: '*',
              dummy: ''
            }, {
              name: 'formOfPayment',
              title: 'Form of Payment',
              type: '_id_195',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'PaymentRule',
              inpColumnName: 'inppaymentrule',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              firstFocusedField: true,
              width: '*',
              dummy: ''
            }, {
              name: 'dueDate',
              title: 'Due Date',
              type: '_id_15',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Dateplanned',
              inpColumnName: 'inpdateplanned',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'businessPartner',
              title: 'Business Partner',
              type: '_id_800057',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'C_BPartner_ID',
              inpColumnName: 'inpcBpartnerId',
              referencedKeyColumnName: 'C_BPartner_ID',
              targetEntity: 'BusinessPartner',
              required: true,
              selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
              popupTextMatchStyle: 'startsWith',
              textMatchStyle: 'startsWith',
              defaultPopupFilterField: 'name',
              displayField: 'name',
              valueField: 'bpid',
              pickListFields: [{
                title: ' ',
                name: 'name',
                disableFilter: true,
                canSort: false,
                type: 'text'
              }, {
                title: 'Location',
                name: 'locationname',
                disableFilter: true,
                canSort: false,
                type: '_id_10'
              }, {
                title: 'Contact',
                name: 'contactname',
                disableFilter: true,
                canSort: false,
                type: '_id_10'
              }],
              showSelectorGrid: true,
              selectorGridFields: [{
                title: 'Name',
                name: 'name',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Value',
                name: 'value',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Credit Line available',
                name: 'creditAvailable',
                disableFilter: false,
                canSort: true,
                type: '_id_12',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Customer Balance',
                name: 'creditUsed',
                disableFilter: false,
                canSort: true,
                type: '_id_12',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }, {
                title: 'Location',
                name: 'locationname',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Contact',
                name: 'contactname',
                disableFilter: false,
                canSort: true,
                type: '_id_10',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBTextFilterItem'
              }, {
                title: 'Customer',
                name: 'customer',
                disableFilter: false,
                canSort: true,
                type: '_id_20',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBYesNoItem'
              }, {
                title: 'Vendor',
                name: 'vendor',
                disableFilter: false,
                canSort: true,
                type: '_id_20',
                filterOnKeypress: true,
                canFilter: true,
                filterEditorType: 'OBYesNoItem'
              }, {
                title: 'Income',
                name: 'income',
                disableFilter: false,
                canSort: true,
                type: '_id_12',
                canFilter: true,
                filterEditorType: 'OBNumberFilterItem'
              }],
              outFields: {
                'id': {
                  'fieldName': '',
                  'suffix': ''
                },
                '_identifier': {
                  'fieldName': '',
                  'suffix': ''
                },
                'locationid': {
                  'fieldName': 'locationid',
                  'suffix': '_LOC'
                },
                'contactid': {
                  'fieldName': 'contactid',
                  'suffix': '_CON'
                }
              },
              extraSearchFields: ['value'],
              optionDataSource: OB.Datasource.create({
                createClassName: '',
                titleField: OB.Constants.IDENTIFIER,
                dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
                recordXPath: '/response/data',
                dataFormat: 'json',
                operationBindings: [{
                  operationType: 'fetch',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'POST'
                  }
                }, {
                  operationType: 'add',
                  dataProtocol: 'postMessage'
                }, {
                  operationType: 'remove',
                  dataProtocol: 'postParams',
                  requestProperties: {
                    httpMethod: 'DELETE'
                  }
                }, {
                  operationType: 'update',
                  dataProtocol: 'postMessage',
                  requestProperties: {
                    httpMethod: 'PUT'
                  }
                }],
                requestProperties: {
                  params: {
                    targetProperty: 'businessPartner',
                    adTabId: '800219',
                    IsSelectorItem: 'true',
                    columnName: 'C_BPartner_ID',
                    _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
                  }
                },
                fields: []
              }),
              whereClause: '',
              outHiddenInputPrefix: 'inpcBpartnerId',
              width: '*',
              dummy: ''
            }, {
              name: 'description',
              title: 'Description',
              type: '_id_14',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 2,
              rowSpan: 2,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'Description',
              inpColumnName: 'inpdescription',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              width: '*',
              dummy: ''
            }, {
              name: '104',
              title: 'Reference',
              type: 'OBSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: true,
              hasDefaultValue: false,
              sectionExpanded: true,
              defaultValue: 'Reference',
              itemIds: ['cashbook', 'cashJournalLine', 'bankAccount', 'bankStatementLine'],
              dummy: ''
            }, {
              name: 'cashbook',
              title: 'Cashbook',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_CashBook_ID',
              inpColumnName: 'inpcCashbookId',
              referencedKeyColumnName: 'C_CashBook_ID',
              targetEntity: 'FinancialMgmtCashBook',
              required: false,
              width: '*',
              dummy: ''
            }, {
              name: 'cashJournalLine',
              title: 'Cash Journal Line',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_CashLine_ID',
              inpColumnName: 'inpcCashlineId',
              referencedKeyColumnName: 'C_CashLine_ID',
              targetEntity: 'FinancialMgmtJournalLine',
              required: false,
              displayField: 'cashJournalLine._identifier',
              valueField: 'cashJournalLine',
              showPickerIcon: true,
              width: '*',
              dummy: ''
            }, {
              name: 'bankAccount',
              title: 'Bank Account',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_BankAccount_ID',
              inpColumnName: 'inpcBankaccountId',
              referencedKeyColumnName: 'C_BankAccount_ID',
              targetEntity: 'BankAccount',
              required: false,
              width: '*',
              dummy: ''
            }, {
              name: 'bankStatementLine',
              title: 'Bank Statement Line',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_BankStatementLine_ID',
              inpColumnName: 'inpcBankstatementlineId',
              referencedKeyColumnName: 'C_BankStatementLine_ID',
              targetEntity: 'FinancialMgmtBankStatementLine',
              required: false,
              displayField: 'bankStatementLine._identifier',
              valueField: 'bankStatementLine',
              showPickerIcon: true,
              width: '*',
              dummy: ''
            }, {
              name: '103',
              title: 'Amounts',
              type: 'OBSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: true,
              hasDefaultValue: false,
              sectionExpanded: true,
              defaultValue: 'Amounts',
              itemIds: ['amount', 'currency', 'writeoffAmount', 'initialStatus', 'project', 'receipt', 'paymentComplete'],
              dummy: ''
            }, {
              name: 'amount',
              title: 'Amount',
              type: '_id_12',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Amount',
              inpColumnName: 'inpamount',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: true,
              "width": "50%",
              dummy: ''
            }, {
              name: 'currency',
              title: 'Currency',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Currency_ID',
              inpColumnName: 'inpcCurrencyId',
              referencedKeyColumnName: 'C_Currency_ID',
              targetEntity: 'Currency',
              required: true,
              width: '*',
              dummy: ''
            }, {
              name: 'writeoffAmount',
              title: 'Write-off Amount',
              type: '_id_12',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'WriteOffAmt',
              inpColumnName: 'inpwriteoffamt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": "50%",
              dummy: ''
            }, {
              name: 'initialStatus',
              title: 'Initial Status',
              type: '_id_800070',
              disabled: false,
              readonly: false,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'Status_Initial',
              inpColumnName: 'inpstatusInitial',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              width: '*',
              dummy: ''
            }, {
              name: 'project',
              title: 'Project',
              type: '_id_19',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'C_Project_ID',
              inpColumnName: 'inpcProjectId',
              referencedKeyColumnName: 'C_Project_ID',
              targetEntity: 'Project',
              required: false,
              width: '*',
              dummy: ''
            }, {
              name: 'receipt',
              title: 'Receipt',
              type: '_id_20',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: true,
              columnName: 'IsReceipt',
              inpColumnName: 'inpisreceipt',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: 'paymentComplete',
              title: 'Payment Complete',
              type: '_id_20',
              disabled: true,
              readonly: true,
              updatable: true,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: true,
              hasDefaultValue: false,
              columnName: 'IsPaid',
              inpColumnName: 'inpispaid',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              "width": 1,
              "overflow": "visible",
              dummy: ''
            }, {
              name: '1000100001',
              title: 'Audit',
              type: 'OBAuditSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'Audit',
              itemIds: ['creationDate', 'createdBy', 'updated', 'updatedBy'],
              dummy: ''
            }, {
              name: 'creationDate',
              title: 'Creation Date',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'createdBy',
              title: 'Created By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: 'updated',
              title: 'Updated',
              type: '_id_16',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: '',
              required: false,
              'width': '*',
              dummy: ''
            }, {
              name: 'updatedBy',
              title: 'Updated By',
              type: '_id_30',
              disabled: true,
              readonly: true,
              updatable: false,
              parentProperty: false,
              colSpan: 1,
              rowSpan: 1,
              startRow: false,
              endRow: false,
              personalizable: false,
              hasDefaultValue: false,
              columnName: '',
              inpColumnName: '',
              referencedKeyColumnName: '',
              targetEntity: 'User',
              required: false,
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              showPickerIcon: true,
              'width': '*',
              dummy: ''
            }, {
              name: '_notes_',
              title: 'dummy',
              type: 'OBNoteSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_notes_Canvas'],
              dummy: ''
            }, {
              name: '_notes_Canvas',
              title: 'dummy',
              type: 'OBNoteCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_linkedItems_',
              title: 'dummy',
              type: 'OBLinkedItemSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_linkedItems_Canvas'],
              dummy: ''
            }, {
              name: '_linkedItems_Canvas',
              title: 'dummy',
              type: 'OBLinkedItemCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }, {
              name: '_attachments_',
              title: 'dummy',
              type: 'OBAttachmentsSectionItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              sectionExpanded: false,
              defaultValue: 'dummy',
              itemIds: ['_attachments_Canvas'],
              dummy: ''
            }, {
              name: '_attachments_Canvas',
              title: '',
              type: 'OBAttachmentCanvasItem',
              disabled: false,
              readonly: false,
              updatable: true,
              parentProperty: false,
              colSpan: 4,
              rowSpan: 1,
              startRow: true,
              endRow: true,
              personalizable: false,
              hasDefaultValue: false,
              dummy: ''
            }],
            statusBarFields: [],
            obFormProperties: {
              onFieldChanged: function (form, item, value) {
                var f = form || this,
                    context = this.view.getContextInfo(false, true),
                    currentValues = f.view.getCurrentValues(),
                    otherItem;
                otherItem = f.getItem('amount');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (currentValues.valid === true) {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
                otherItem = f.getItem('currency');
                if (otherItem && otherItem.disable && otherItem.enable) {
                  if (f.readOnly) {
                    otherItem.disable();
                  } else if (currentValues.valid === true) {
                    otherItem.disable();
                  } else {
                    otherItem.enable();
                  }
                }
              }
            }
          });
          this.viewGrid = isc.OBViewGrid.create({
            uiPattern: 'STD',
            fields: [{
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'C_Settlement_Cancel_ID',
                inpColumnName: 'inpcSettlementCancelId',
                referencedKeyColumnName: 'C_Settlement_ID',
                targetEntity: 'FinancialMgmtSettlement',
                disabled: true,
                readonly: true,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'settlementCancelled._identifier',
              valueField: 'settlementCancelled',
              foreignKeyField: true,
              name: 'settlementCancelled',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'settlementCancelled')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Settlement Cancelled',
              prompt: 'Settlement Cancelled',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Settlement_Cancel_ID',
              inpColumnName: 'inpcSettlementCancelId',
              referencedKeyColumnName: 'C_Settlement_ID',
              targetEntity: 'FinancialMgmtSettlement'
            }, {
              autoExpand: false,
              type: '_id_195',
              editorProperties: {
                width: '*',
                columnName: 'PaymentRule',
                inpColumnName: 'inppaymentrule',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                firstFocusedField: true
              },
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'formOfPayment')])",
              width: isc.OBGrid.getDefaultColumnWidth(21),
              name: 'formOfPayment',
              canExport: true,
              canHide: true,
              editorType: 'OBListItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBListFilterItem',
              title: 'Form of Payment',
              prompt: 'Form of Payment',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'PaymentRule',
              inpColumnName: 'inppaymentrule',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_15',
              cellAlign: 'left',
              editorProperties: {
                "width": "50%",
                columnName: 'Dateplanned',
                inpColumnName: 'inpdateplanned',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'dueDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBMiniDateRangeItem',
              title: 'Due Date',
              prompt: 'Due Date',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Dateplanned',
              inpColumnName: 'inpdateplanned',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_800057',
              editorProperties: {
                selectorDefinitionId: '862F54CB1B074513BD791C6789F4AA42',
                popupTextMatchStyle: 'startsWith',
                textMatchStyle: 'startsWith',
                defaultPopupFilterField: 'name',
                displayField: 'name',
                valueField: 'bpid',
                pickListFields: [{
                  title: ' ',
                  name: 'name',
                  disableFilter: true,
                  canSort: false,
                  type: 'text'
                }, {
                  title: 'Location',
                  name: 'locationname',
                  disableFilter: true,
                  canSort: false,
                  type: '_id_10'
                }, {
                  title: 'Contact',
                  name: 'contactname',
                  disableFilter: true,
                  canSort: false,
                  type: '_id_10'
                }],
                showSelectorGrid: true,
                selectorGridFields: [{
                  title: 'Name',
                  name: 'name',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Value',
                  name: 'value',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Credit Line available',
                  name: 'creditAvailable',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_12',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Customer Balance',
                  name: 'creditUsed',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_12',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }, {
                  title: 'Location',
                  name: 'locationname',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Contact',
                  name: 'contactname',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_10',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBTextFilterItem'
                }, {
                  title: 'Customer',
                  name: 'customer',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_20',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem'
                }, {
                  title: 'Vendor',
                  name: 'vendor',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_20',
                  filterOnKeypress: true,
                  canFilter: true,
                  filterEditorType: 'OBYesNoItem'
                }, {
                  title: 'Income',
                  name: 'income',
                  disableFilter: false,
                  canSort: true,
                  type: '_id_12',
                  canFilter: true,
                  filterEditorType: 'OBNumberFilterItem'
                }],
                outFields: {
                  'id': {
                    'fieldName': '',
                    'suffix': ''
                  },
                  '_identifier': {
                    'fieldName': '',
                    'suffix': ''
                  },
                  'locationid': {
                    'fieldName': 'locationid',
                    'suffix': '_LOC'
                  },
                  'contactid': {
                    'fieldName': 'contactid',
                    'suffix': '_CON'
                  }
                },
                extraSearchFields: ['value'],
                optionDataSource: OB.Datasource.create({
                  createClassName: '',
                  titleField: OB.Constants.IDENTIFIER,
                  dataURL: '/openbravo/org.openbravo.service.datasource/F8DD408F2F3A414188668836F84C21AF',
                  recordXPath: '/response/data',
                  dataFormat: 'json',
                  operationBindings: [{
                    operationType: 'fetch',
                    dataProtocol: 'postParams',
                    requestProperties: {
                      httpMethod: 'POST'
                    }
                  }, {
                    operationType: 'add',
                    dataProtocol: 'postMessage'
                  }, {
                    operationType: 'remove',
                    dataProtocol: 'postParams',
                    requestProperties: {
                      httpMethod: 'DELETE'
                    }
                  }, {
                    operationType: 'update',
                    dataProtocol: 'postMessage',
                    requestProperties: {
                      httpMethod: 'PUT'
                    }
                  }],
                  requestProperties: {
                    params: {
                      targetProperty: 'businessPartner',
                      adTabId: '800219',
                      IsSelectorItem: 'true',
                      columnName: 'C_BPartner_ID',
                      _extraProperties: 'contactname,value,creditAvailable,locationid,contactid,creditUsed,name,customer,bpid,locationname,vendor,income'
                    }
                  },
                  fields: []
                }),
                whereClause: '',
                outHiddenInputPrefix: 'inpcBpartnerId',
                width: '*',
                columnName: 'C_BPartner_ID',
                inpColumnName: 'inpcBpartnerId',
                referencedKeyColumnName: 'C_BPartner_ID',
                targetEntity: 'BusinessPartner',
                disabled: true,
                readonly: true,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'businessPartner._identifier',
              valueField: 'businessPartner',
              foreignKeyField: true,
              name: 'businessPartner',
              canExport: true,
              canHide: true,
              editorType: 'OBSelectorItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'businessPartner')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Business Partner',
              prompt: 'Business Partner',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_BPartner_ID',
              inpColumnName: 'inpcBpartnerId',
              referencedKeyColumnName: 'C_BPartner_ID',
              targetEntity: 'BusinessPartner'
            }, {
              autoExpand: true,
              type: '_id_14',
              editorProperties: {
                width: '*',
                columnName: 'Description',
                inpColumnName: 'inpdescription',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'description')])",
              width: isc.OBGrid.getDefaultColumnWidth(255),
              name: 'description',
              canExport: true,
              canHide: true,
              editorType: 'OBPopUpTextAreaItem',
              canSort: false,
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBTextItem',
              title: 'Description',
              prompt: 'Description',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Description',
              inpColumnName: 'inpdescription',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_CashBook_ID',
                inpColumnName: 'inpcCashbookId',
                referencedKeyColumnName: 'C_CashBook_ID',
                targetEntity: 'FinancialMgmtCashBook',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'cashbook._identifier',
              valueField: 'cashbook',
              foreignKeyField: true,
              name: 'cashbook',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'cashbook')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Cashbook',
              prompt: 'Cashbook',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_CashBook_ID',
              inpColumnName: 'inpcCashbookId',
              referencedKeyColumnName: 'C_CashBook_ID',
              targetEntity: 'FinancialMgmtCashBook'
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'C_CashLine_ID',
                inpColumnName: 'inpcCashlineId',
                referencedKeyColumnName: 'C_CashLine_ID',
                targetEntity: 'FinancialMgmtJournalLine',
                disabled: true,
                readonly: true,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'cashJournalLine._identifier',
              valueField: 'cashJournalLine',
              foreignKeyField: true,
              name: 'cashJournalLine',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'cashJournalLine')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Cash Journal Line',
              prompt: 'Cash Journal Line',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_CashLine_ID',
              inpColumnName: 'inpcCashlineId',
              referencedKeyColumnName: 'C_CashLine_ID',
              targetEntity: 'FinancialMgmtJournalLine'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_BankAccount_ID',
                inpColumnName: 'inpcBankaccountId',
                referencedKeyColumnName: 'C_BankAccount_ID',
                targetEntity: 'BankAccount',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'bankAccount._identifier',
              valueField: 'bankAccount',
              foreignKeyField: true,
              name: 'bankAccount',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'bankAccount')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Bank Account',
              prompt: 'Bank Account',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_BankAccount_ID',
              inpColumnName: 'inpcBankaccountId',
              referencedKeyColumnName: 'C_BankAccount_ID',
              targetEntity: 'BankAccount'
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'C_BankStatementLine_ID',
                inpColumnName: 'inpcBankstatementlineId',
                referencedKeyColumnName: 'C_BankStatementLine_ID',
                targetEntity: 'FinancialMgmtBankStatementLine',
                disabled: true,
                readonly: true,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'bankStatementLine._identifier',
              valueField: 'bankStatementLine',
              foreignKeyField: true,
              name: 'bankStatementLine',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'bankStatementLine')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Bank Statement Line',
              prompt: 'Bank Statement Line',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_BankStatementLine_ID',
              inpColumnName: 'inpcBankstatementlineId',
              referencedKeyColumnName: 'C_BankStatementLine_ID',
              targetEntity: 'FinancialMgmtBankStatementLine'
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'Amount',
                inpColumnName: 'inpamount',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'amount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Amount',
              prompt: 'Amount',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Amount',
              inpColumnName: 'inpamount',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Currency_ID',
                inpColumnName: 'inpcCurrencyId',
                referencedKeyColumnName: 'C_Currency_ID',
                targetEntity: 'Currency',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'currency._identifier',
              valueField: 'currency',
              foreignKeyField: true,
              name: 'currency',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'currency')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Currency',
              prompt: 'Currency',
              required: true,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Currency_ID',
              inpColumnName: 'inpcCurrencyId',
              referencedKeyColumnName: 'C_Currency_ID',
              targetEntity: 'Currency'
            }, {
              autoExpand: false,
              type: '_id_12',
              editorProperties: {
                "width": "50%",
                columnName: 'WriteOffAmt',
                inpColumnName: 'inpwriteoffamt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: true
              },
              name: 'writeoffAmount',
              canExport: true,
              canHide: true,
              editorType: 'OBNumberItem',
              canFilter: true,
              filterEditorType: 'OBNumberFilterItem',
              title: 'Write-off Amount',
              prompt: 'Write-off Amount',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'WriteOffAmt',
              inpColumnName: 'inpwriteoffamt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_800070',
              editorProperties: {
                width: '*',
                columnName: 'Status_Initial',
                inpColumnName: 'inpstatusInitial',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: false
              },
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'initialStatus')])",
              width: isc.OBGrid.getDefaultColumnWidth(21),
              name: 'initialStatus',
              canExport: true,
              canHide: true,
              editorType: 'OBListItem',
              filterOnKeypress: false,
              canFilter: true,
              filterEditorType: 'OBListFilterItem',
              title: 'Initial Status',
              prompt: 'Initial Status',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'Status_Initial',
              inpColumnName: 'inpstatusInitial',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'C_Project_ID',
                inpColumnName: 'inpcProjectId',
                referencedKeyColumnName: 'C_Project_ID',
                targetEntity: 'Project',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'project._identifier',
              valueField: 'project',
              foreignKeyField: true,
              name: 'project',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'project')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Project',
              prompt: 'Project',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'C_Project_ID',
              inpColumnName: 'inpcProjectId',
              referencedKeyColumnName: 'C_Project_ID',
              targetEntity: 'Project'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsReceipt',
                inpColumnName: 'inpisreceipt',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: true
              },
              name: 'receipt',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Receipt',
              prompt: 'Receipt',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'IsReceipt',
              inpColumnName: 'inpisreceipt',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsPaid',
                inpColumnName: 'inpispaid',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: true,
                readonly: true,
                updatable: true
              },
              name: 'paymentComplete',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Payment Complete',
              prompt: 'Payment Complete',
              required: false,
              escapeHTML: true,
              showIf: 'true',
              columnName: 'IsPaid',
              inpColumnName: 'inpispaid',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_800059',
              editorProperties: {
                "width": "",
                "searchUrl": "\/info\/Invoice.html",
                "inFields": [{
                  "columnName": "inpadOrgId",
                  "parameterName": "inpAD_Org_ID"
                }],
                "outFields": [],
                columnName: 'C_Invoice_ID',
                inpColumnName: 'inpcInvoiceId',
                referencedKeyColumnName: 'C_Invoice_ID',
                targetEntity: 'Invoice',
                disabled: true,
                readonly: true,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'invoice._identifier',
              valueField: 'invoice',
              foreignKeyField: true,
              name: 'invoice',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'invoice')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Invoice',
              prompt: 'Invoice',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_Invoice_ID',
              inpColumnName: 'inpcInvoiceId',
              referencedKeyColumnName: 'C_Invoice_ID',
              targetEntity: 'Invoice'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsValid',
                inpColumnName: 'inpisvalid',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true,
                redrawOnChange: true,
                changed: function (form, item, value) {
                  if (this.pickValue && !this._pickedValue) {
                    return;
                  }
                  this.Super('changed', arguments);
                  form.onFieldChanged(form, item, value);
                  form.view.toolBar.refreshCustomButtonsView(form.view);
                }
              },
              name: 'valid',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Valid',
              prompt: 'Valid',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsValid',
              inpColumnName: 'inpisvalid',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsManual',
                inpColumnName: 'inpismanual',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'manual',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Manual',
              prompt: 'Manual',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsManual',
              inpColumnName: 'inpismanual',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'Generate_Processed',
                inpColumnName: 'inpgenerateProcessed',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'generateProcessed',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Generate_Processed',
              prompt: 'Generate_Processed',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Generate_Processed',
              inpColumnName: 'inpgenerateProcessed',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_30',
              editorProperties: {
                width: '',
                columnName: 'C_Settlement_Generate_ID',
                inpColumnName: 'inpcSettlementGenerateId',
                referencedKeyColumnName: 'C_Settlement_ID',
                targetEntity: 'FinancialMgmtSettlement',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'settlementGenerate._identifier',
              valueField: 'settlementGenerate',
              foreignKeyField: true,
              name: 'settlementGenerate',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'settlementGenerate')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'C_Settlement_Generate_ID',
              prompt: 'C_Settlement_Generate_ID',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'C_Settlement_Generate_ID',
              inpColumnName: 'inpcSettlementGenerateId',
              referencedKeyColumnName: 'C_Settlement_ID',
              targetEntity: 'FinancialMgmtSettlement'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'Cancel_Processed',
                inpColumnName: 'inpcancelProcessed',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'cancelProcessed',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Cancel processed',
              prompt: 'Cancel processed',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'Cancel_Processed',
              inpColumnName: 'inpcancelProcessed',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsActive',
                inpColumnName: 'inpisactive',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'active',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Active',
              prompt: 'Active',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsActive',
              inpColumnName: 'inpisactive',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Org_ID',
                inpColumnName: 'inpadOrgId',
                referencedKeyColumnName: 'AD_Org_ID',
                targetEntity: 'Organization',
                disabled: false,
                readonly: false,
                updatable: true
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'organization._identifier',
              valueField: 'organization',
              foreignKeyField: true,
              name: 'organization',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'organization')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Organization',
              prompt: 'Organization',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Org_ID',
              inpColumnName: 'inpadOrgId',
              referencedKeyColumnName: 'AD_Org_ID',
              targetEntity: 'Organization'
            }, {
              autoExpand: true,
              type: '_id_19',
              editorProperties: {
                displayField: null,
                valueField: null,
                columnName: 'AD_Client_ID',
                inpColumnName: 'inpadClientId',
                referencedKeyColumnName: 'AD_Client_ID',
                targetEntity: 'ADClient',
                disabled: true,
                readonly: true,
                updatable: false
              },
              width: isc.OBGrid.getDefaultColumnWidth(44),
              displayField: 'client._identifier',
              valueField: 'client',
              foreignKeyField: true,
              name: 'client',
              canExport: true,
              canHide: true,
              editorType: 'OBFKItem',
              showHover: true,
              hoverHTML: "return grid.getDisplayValue(colNum, record[(this.displayField ? this.displayField : 'client')])",
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBFKFilterTextItem',
              title: 'Client',
              prompt: 'Client',
              required: true,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'AD_Client_ID',
              inpColumnName: 'inpadClientId',
              referencedKeyColumnName: 'AD_Client_ID',
              targetEntity: 'ADClient'
            }, {
              autoExpand: false,
              type: '_id_20',
              editorProperties: {
                "width": 1,
                "overflow": "visible",
                "showTitle": false,
                "showLabel": false,
                columnName: 'IsAutomaticGenerated',
                inpColumnName: 'inpisautomaticgenerated',
                referencedKeyColumnName: '',
                targetEntity: '',
                disabled: false,
                readonly: false,
                updatable: true
              },
              name: 'isAutomaticGenerated',
              canExport: true,
              canHide: true,
              editorType: 'OBCheckboxItem',
              width: '*',
              autoFitWidth: false,
              formatCellValue: function (value, record, rowNum, colNum, grid) {
                return OB.Utilities.getYesNoDisplayValue(value);
              },
              filterOnKeypress: true,
              canFilter: true,
              filterEditorType: 'OBYesNoItem',
              title: 'Is Automatic Generated',
              prompt: 'Is Automatic Generated',
              required: false,
              escapeHTML: true,
              showIf: 'false',
              columnName: 'IsAutomaticGenerated',
              inpColumnName: 'inpisautomaticgenerated',
              referencedKeyColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'creationDate',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'creationDate',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Creation Date',
              prompt: 'Creation Date',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'creationDate',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'createdBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'createdBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'createdBy._identifier',
              valueField: 'createdBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Created By',
              prompt: 'Created By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'createdBy',
              inpColumnName: '',
              targetEntity: 'User'
            }, {
              autoExpand: false,
              type: '_id_16',
              editorProperties: {
                width: '*',
                columnName: 'updated',
                targetEntity: '',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updated',
              canExport: true,
              canHide: true,
              editorType: 'OBDateItem',
              filterEditorType: 'OBMiniDateRangeItem',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated',
              prompt: 'Updated',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updated',
              inpColumnName: '',
              targetEntity: ''
            }, {
              autoExpand: false,
              type: '_id_30',
              editorProperties: {
                width: '*',
                columnName: 'updatedBy',
                targetEntity: 'User',
                disabled: true,
                updatable: false
              },
              showHover: false,
              width: isc.OBGrid.getDefaultColumnWidth(30),
              name: 'updatedBy',
              canExport: true,
              canHide: true,
              editorType: 'OBSearchItem',
              filterEditorType: 'OBFKFilterTextItem',
              displayField: 'updatedBy._identifier',
              valueField: 'updatedBy',
              filterOnKeypress: true,
              canFilter: true,
              required: false,
              title: 'Updated By',
              prompt: 'Updated By',
              escapeHTML: true,
              showIf: 'false',
              columnName: 'updatedBy',
              inpColumnName: '',
              targetEntity: 'User'
            }],
            autoExpandFieldNames: ['description', 'settlementCancelled', 'businessPartner', 'cashbook', 'cashJournalLine', 'bankAccount', 'bankStatementLine', 'currency', 'project', 'invoice', 'settlementGenerate', 'organization', 'client'],
            whereClause: '',
            orderByClause: '',
            sortField: 'formOfPayment',
            filterClause: '',
            filterName: '',
            foreignKeyFieldNames: ['settlementCancelled', 'businessPartner', 'cashbook', 'cashJournalLine', 'bankAccount', 'bankStatementLine', 'currency', 'project', 'invoice', 'settlementGenerate', 'organization', 'client']
          });
          this.Super('initWidget', arguments);
        },
        createViewStructure: function () {}
      }));
    }
  }
});