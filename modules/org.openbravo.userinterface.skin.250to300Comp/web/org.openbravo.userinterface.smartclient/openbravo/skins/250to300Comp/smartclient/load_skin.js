/*============================================================
    "Enterprise" theme programmatic settings
    Copyright 2003 and beyond, Isomorphic Software
============================================================*/


isc.loadSkin = function (theWindow) {
if (theWindow == null) theWindow = window;
with (theWindow) {


//----------------------------------------
// Specify skin directory
//----------------------------------------
    // must be relative to your application file or isomorphicDir
    isc.Page.setSkinDir("[ISOMORPHIC]/../openbravo/skins/250to300Comp/smartclient/")

//----------------------------------------
// Load skin style sheet(s)
//----------------------------------------
    //isc.Page.loadStyleSheet("[SKIN]/skin_styles.css?" +  OB.Application.moduleVersionParameters['EC356CEE3D46416CA1EBEEB9AB82EDB9'], theWindow)

//============================================================
//  Component Skinning
//============================================================
//   1) Scrollbars
//   2) Buttons
//   3) Resizebars
//   4) Sections
//   5) Progressbars
//   6) TabSets
//   7) Windows
//   8) Dialogs
//   9) Pickers
//  10) Menus
//  11) Hovers
//  12) ListGrids
//  13) TreeGrids
//  14) Form controls
//  15) Drag & Drop
//  16) Edges
//  17) Sliders
//  18) TileList
//  19) CubeGrid
//  20) FilterBuilder
//  21) Printing
//============================================================


	isc.Canvas.addProperties({
		groupBorderCSS: "1px solid #A7ABB4"
	});

//----------------------------------------
// 1) Scrollbars
//----------------------------------------
    isc.Canvas.addProperties({
        showCustomScrollbars:true,
        scrollbarSize:16,
		cornerSize: 16
    })
    isc.ScrollThumb.addProperties({
        capSize:4,
        vSrc:"[SKIN]vthumb.png",
        hSrc:"[SKIN]hthumb.png",
		showRollOver: true,
		showDown: true,
        backgroundColor:"transparent"
    })
    isc.Scrollbar.addProperties({
        btnSize:19,
        showRollOver:true,
        thumbMinSize:20,
        thumbInset:1,
        thumbOverlap:2,
        backgroundColor:"#FFFFFF",
        vSrc:"[SKIN]vscroll.gif",
        hSrc:"[SKIN]hscroll.gif"
    })


//----------------------------------------
// 2) Buttons
//----------------------------------------
    
    // "IButton" is the new standard button class for SmartClient applications. Application
    // code should use IButton instead of Button for all standalone buttons. Other skins may
    // map IButton directly to Button, so this single class will work everywhere. Button remains
    // for internal and advanced uses (eg if you need to mix both CSS-based and image-based
    // standalone buttons in the same application).
    isc.defineClass("IButton", "StretchImgButton").addProperties({
        src:"[SKIN]button/button.png",
        height:20,
        width:100,
        capSize:4,
        vertical:false,
        titleStyle:"buttonTitle",
        showFocused:true,
        showFocusedAsOver:false
    });

    isc.defineClass("IAutoFitButton", "IButton").addProperties({
        autoFit: true,
        autoFitDirection: isc.Canvas.HORIZONTAL
    });

    isc.ImgButton.addProperties({
        showFocused: true,
        showFocusedAsOver:false
    });
	
	isc.Button.addProperties({
		height:20,
		showFocused: true,
		showFocusedAsOver: false
	});
	
	isc.Label.addProperties({
		showFocused: false
	});



//----------------------------------------
// 3) Resizebars
//----------------------------------------
    // StretchImgSplitbar class renders as resize bar with 
    // end caps, body, grip
    isc.StretchImgSplitbar.addProperties({
        // modify vSrc / hSrc for custom appearance
        //vSrc:"[SKIN]vsplit.gif",
        //hSrc:"[SKIN]hsplit.gif",
        capSize:10,
        showGrip:true
    })
    
    // ImgSplitbar renders as resizebar with resize grip only
    isc.ImgSplitbar.addProperties({
        // modify these properties for custom appearance
        //vSrc:"[SKIN]vgrip.png",
        //hSrc:"[SKIN]hgrip.png",
        //showDown:true,
        //styleName:"splitbar"
    })
    
    isc.Snapbar.addProperties({
        vSrc:"[SKIN]vsplit.png",
        hSrc:"[SKIN]hsplit.png",
        baseStyle:"splitbar",
	    items : [
    	    {name:"blank", width:"capSize", height:"capSize"},
    		{name:"blank", width:"*", height:"*"},
	    	{name:"blank", width:"capSize", height:"capSize"}
        ],
        showDownGrip:false,
        gripBreadth:6,
        gripLength:26,
        capSize:8
    })
    
    isc.Layout.addProperties({
        resizeBarSize:9,
        // Use the Snapbar as a resizeBar by default - subclass of Splitbar that 
        // shows interactive (closed/open) grip images
        // Other options include the Splitbar, StretchImgSplitbar or ImgSplitbar
        resizeBarClass:"Snapbar"
    })

    
//----------------------------------------
// 4) Sections
//----------------------------------------
    if (isc.SectionItem) {
        isc.SectionItem.addProperties({
            sectionHeaderClass:"ImgSectionHeader",
            height:22
        })
    }
    if (isc.SectionStack) {
        isc.SectionStack.addProperties({
            backgroundColor:null,
            sectionHeaderClass:"ImgSectionHeader",
            headerHeight:22
        })
        isc.ImgSectionHeader.changeDefaults("backgroundDefaults", {
            showRollOver:true,
            showDown:false,
            showDisabledIcon:true,
            showRollOverIcon:true,
            src:"[SKIN]SectionHeader/header.png",
            icon:"[SKIN]SectionHeader/opener.png",
			iconSize: 17,
            capSize:3,
            titleStyle:"imgSectionHeaderTitle",
            baseStyle:"imgSectionHeader",
            backgroundColor:"transparent"
        })
        isc.SectionHeader.addProperties({
            icon:"[SKIN]SectionHeader/opener.png",
			iconSize: 16
        })
    }


//----------------------------------------
// 5) Progressbars
//----------------------------------------
    if (isc.Progressbar) {
        isc.Progressbar.addProperties({
            horizontalItems: [
            {name:"bar_start",size:3},
            {name:"bar_stretch",size:0},
            {name:"bar_end",size:4},
            {name:"empty_start",size:2},
            {name:"empty_stretch",size:0},
            {name:"empty_end",size:2}
            ],
            breadth:12
        })
    }


//----------------------------------------
// 6) TabSets
//----------------------------------------
    if (isc.TabSet) {
        isc.TabSet.addProperties({
            tabBarThickness:20,
            scrollerButtonSize:19,
            pickerButtonSize:20,
       
            symmetricScroller:false,
            symmetricPickerButton:false,

            scrollerSrc:"[SKIN]scroll.png",
            pickerButtonSrc:"[SKIN]picker.png",

            closeTabIconSize:10,

            showEdges:false,
            paneContainerClassName:"tabSetContainer",
            
            paneMargin:5,
            
            showScrollerRollOver: true
        });
        isc.TabSet.changeDefaults("paneContainerDefaults", {
            showEdges:false
        })
        isc.TabBar.addProperties({
            membersMargin:1,
            
            styleName:"tabBar",

            // have the baseline overlap the top edge of the TabSet, using rounded media
            baseLineConstructor:"Canvas",
            baseLineProperties : { 
                backgroundColor: "#C0C3C7",
                overflow:"hidden",
                height:1
            },
            //baseLineSrc:"[SKIN]baseline.png",
            //baseLineCapSize:2,
            baseLineThickness:1

        })
    }    
    if (isc.ImgTab) {
        isc.ImgTab.addProperties({
            src:"[SKIN]tab.png",
            capSize:5,
            showRollOver:true,
            showDown:false,
            showDisabled:true,
            showDisabledIcon:false,
            titleStyle:"tabTitle"
        })
    }


//----------------------------------------
// 7) Windows
//----------------------------------------
    if (isc.Window) {
        isc.Window.addProperties({
            // rounded frame edges
            showEdges:true,
            edgeImage: "[SKINIMG]Window/window.png",
            customEdges:null,
            edgeSize:6,
            edgeTop:23,
            edgeBottom:5,
			edgeOffsetTop:2,
			edgeOffsetRight:5,
			edgeOffsetBottom:5,
            showHeaderBackground:false, // part of edges
            showHeaderIcon:true,

            // clear backgroundColor and style since corners are rounded
            backgroundColor:null,
			border: null,
            styleName:"normal",
            edgeCenterBackgroundColor:"#FFFFFF",
            bodyColor:"transparent",
            bodyStyle:"windowBody",

            layoutMargin:0,
            membersMargin:0,

            showFooter:false,

            showShadow:false,
            shadowDepth:5
        })

        isc.Window.changeDefaults("headerDefaults", {
            layoutMargin:0,
            height:20
        })

        isc.Window.changeDefaults("headerIconDefaults", {
            width:16,
            height:16,
            src:"[SKIN]/Window/headerIcon.png"
        })
        isc.Window.changeDefaults("restoreButtonDefaults", {
             src:"[SKIN]/Window/restore.png",
             showRollOver:true,
             showDown:false,
             width:16,
             height:16
        })
        isc.Window.changeDefaults("closeButtonDefaults", { 
             src:"[SKIN]/Window/close.png",
             showRollOver:true,
             showDown:false,
             width:16,
             height:16
        })
        isc.Window.changeDefaults("maximizeButtonDefaults", { 
             src:"[SKIN]/Window/maximize.png",
             showRollOver:true,
             width:16,
             height:16
        })
        isc.Window.changeDefaults("minimizeButtonDefaults", { 
             src:"[SKIN]/Window/minimize.png",
             showRollOver:true,
             showDown:false,
             width:16,
             height:16
        })
        isc.Window.changeDefaults("toolbarDefaults", {
            buttonConstructor: "IButton"
        })
        
        isc.ColorPicker.addProperties({
            layoutMargin:0
        })

//----------------------------------------
// 8) Dialogs
//----------------------------------------
        if (isc.Dialog) {
            isc.Dialog.addProperties({
                bodyColor:"transparent",
                hiliteBodyColor:"transparent"
            })
            // even though Dialog inherits from Window, we need a separate changeDefaults block
            // because Dialog defines its own toolbarDefaults
            isc.Dialog.changeDefaults("toolbarDefaults", {
                buttonConstructor: "IButton",
                height:42, // 10px margins + 22px button
                membersMargin:10
            })
            if (isc.Dialog.Warn && isc.Dialog.Warn.toolbarDefaults) {
                isc.addProperties(isc.Dialog.Warn.toolbarDefaults, {
                    buttonConstructor: "IButton",
                    height:42,
                    membersMargin:10
                })
            }
        }
        
    } // end isc.Window


//----------------------------------------
// 9) Pickers
//----------------------------------------
    // add bevels and shadows to all pickers
    isc.__pickerDefaults = {
        showEdges:true,
        edgeSize:6,
        edgeImage:"[SKIN]/rounded/frame/FFFFFF/6.png",
        backgroundColor:"#FFFFFF",
        showShadow:true,
        shadowDepth:6,
        shadowOffset:5
    }
    if (isc.ButtonTable) {
        isc.ButtonTable.addProperties({
            backgroundColor:"#FFFFFF"
        })
    }
    if (isc.FormItem) {
        isc.FormItem.changeDefaults("pickerDefaults", isc.__pickerDefaults)
    }
    if (isc.CheckboxItem) {
        isc.CheckboxItem.addProperties({
            checkedImage:"[SKINIMG]/DynamicForm/checked.png",
            uncheckedImage:"[SKINIMG]/DynamicForm/unchecked.png",
            unsetImage:"[SKINIMG]/DynamicForm/unsetcheck.png",
            valueIconWidth:14,
            valueIconHeight:14
        })
    }
    
    if (isc.DateChooser) {
        isc.DateChooser.addProperties({
            headerStyle:"dateChooserButton",
            weekendHeaderStyle:"dateChooserWeekendButton",
            baseNavButtonStyle:"dateChooserNavButton",
            baseWeekdayStyle:"dateChooserWeekday",
            baseWeekendStyle:"dateChooserWeekend",
            baseBottomButtonStyle:"dateChooserBottomButton",
            alternateWeekStyles:false,
        
            showEdges:true,
            edgeImage:"[SKINIMG]/DateChooser/window.png",
            edgeSize:4,
            edgeBottom:20,
            todayButtonHeight:20,
            edgeTop:22,
            edgeOffset:2,
			
            headerHeight:19,

            edgeCenterBackgroundColor:"#FFFFFF",
            backgroundColor:null,
    
            showShadow:true,
            shadowDepth:6,
            shadowOffset:5,

            showDoubleYearIcon:false,
            skinImgDir:"images/DateChooser/",
            prevYearIcon:"[SKIN]doubleArrow_left.png",
            prevYearIconWidth:16,
            prevYearIconHeight:16,
            nextYearIcon:"[SKIN]doubleArrow_right.png",
            nextYearIconWidth:16,    
            nextYearIconHeight:16,
            prevMonthIcon:"[SKIN]arrow_left.png",
            prevMonthIconWidth:16,
            prevMonthIconHeight:16,
            nextMonthIcon:"[SKIN]arrow_right.png",
            nextMonthIconWidth:16,
            nextMonthIconHeight:16
        });
    }
    if (isc.MultiFilePicker) {
        isc.MultiFilePicker.addProperties({
            backgroundColor:"#C7C7C7"
        })
    }
    if (isc.RelationPicker) {
        isc.RelationPicker.addProperties({
            backgroundColor:"#C7C7C7"    
        })
    }


//----------------------------------------
// 10) Menus
//----------------------------------------
    if (isc.Menu) {
        isc.Menu.addProperties({
            cellHeight:22,
            showShadow:true,
            shadowDepth:5,
            showEdges:false,
            submenuImage:{src:"[SKIN]submenu.gif", height:7, width:7},
            submenuDisabledImage:{src:"[SKIN]submenu_disabled.gif", height:7, width:7},
	        checkmarkImage:{src:"[SKIN]check.gif", width:9, height:9},
	        checkmarkDisabledImage:{src:"[SKIN]check_disabled.gif", width:9, height:9},
            bodyStyleName:"gridBody",
			iconBodyStyleName:"menuMain",
            bodyBackgroundColor:null
        });
		isc.addProperties(isc.Menu.ICON_FIELD, {
			width:24,
			baseStyle:"menuIconField"
		});
		isc.Menu.TITLE_FIELD.baseStyle = "menuTitleField";
    }
    
    if (isc.MenuButton) {
        isc.MenuButton.addProperties({
			baseStyle: "button",
            menuButtonImage:"[SKIN]menu_button.gif",
            menuButtonImageUp:"[SKIN]menu_button_up.gif",
            iconWidth:7,
            iconHeight:7
        });
    }
	
	if (isc.SelectionTreeMenu) {
		isc.SelectionTreeMenu.addProperties({
			showIcons:false,
			showKeys:false,
            bodyStyleName:"treeMenuBody",
            bodyBackgroundColor:null
		});
	}


//----------------------------------------
// 11) Hovers
//----------------------------------------
    if (isc.Hover) {
        isc.addProperties(isc.Hover.hoverCanvasDefaults, {
            showShadow:true,
            shadowDepth:5
        })
    }


//----------------------------------------
// 12) ListGrids
//----------------------------------------
    if (isc.ListGrid) {										  
        isc.ListGrid.addProperties({
			tallBaseStyle: "tallCell",
            
            // Render header buttons out as StretchImgButtons
            headerButtonConstructor:"ImgButton",
            sorterConstructor:"ImgButton",
            headerMenuButtonConstructor:"ImgButton",
            
            sortAscendingImage:{src:"[SKIN]sort_ascending.png", width:8, height:6},
            sortDescendingImage:{src:"[SKIN]sort_descending.png", width:8, height:6},
            
            backgroundColor:null, bodyBackgroundColor:null,

            headerHeight:21,
            headerBackgroundColor:null,
            headerBarStyle:"headerBar",
            headerBaseStyle:"headerButton",	// bgcolor tint and borders
            headerTitleStyle:"headerTitle",
            
            bodyStyleName:"gridBody",
            alternateBodyStyleName:"alternateGridBody",
            
            showHeaderMenuButton:true,
            headerMenuButtonBaseStyle:"headerButton",
            headerMenuButtonTitleStyle:"headerTitle",
            
            headerMenuButtonIcon:"[SKIN]/ListGrid/headerMenuButton_icon.png",
            headerMenuButtonIconWidth:8,
            headerMenuButtonIconHeight:6,
            
            groupIcon: "[SKINIMG]/ListGrid/group.gif"
        })
        isc.ListGrid.changeDefaults("sorterDefaults", { 
            // baseStyle / titleStyle is auto-assigned from headerBaseStyle
            showFocused:false,
            src:"[SKIN]ListGrid/header.png",
            baseStyle:"sorterButton"
        })
        isc.ListGrid.changeDefaults("headerButtonDefaults", {
            showTitle:true,
            showDown:false,
            showFocused:false,
            // baseStyle / titleStyle is auto-assigned from headerBaseStyle
            src:"[SKIN]ListGrid/header.png"
        })
        isc.ListGrid.changeDefaults("headerMenuButtonDefaults", {
            showDown:false,
            showTitle:true,
            src:"[SKIN]ListGrid/header.png"
        })
    }

//----------------------------------------
// 13) TreeGrids
//----------------------------------------
    if (isc.TreeGrid) {
        isc.TreeGrid.addProperties({
			openerIconSize: 20,
            folderIcon:"[SKIN]folder.png",
            nodeIcon:"[SKIN]file.png",
            manyItemsImage:"[SKIN]folder_file.png"
        })
    }


//----------------------------------------
// 14) Form controls
//----------------------------------------
    if (isc.FormItem) {isc.FormItem.addProperties({
        defaultIconSrc:"[SKIN]/controls/helper_control.gif",
        iconHeight:18,
        iconWidth:18,
        iconVAlign:"middle"
        
    })}
    
    if (isc.TextItem) {isc.TextItem.addProperties({
        height:isc.Browser.isSafari ? 22 : 20,
        showFocused: true
    })}
    
    if (isc.TextAreaItem) {isc.TextAreaItem.addProperties({
        showFocused: true     
    })}
    
    if (isc.SelectItem) {isc.SelectItem.addProperties({            
        textBoxStyle:"selectItemText",
        showFocusedPickerIcon:true,
        pickerIconSrc:"[SKIN]/controls/selectPicker.gif",
        height:20,
        pickerIconWidth:18
    })}
    
    if (isc.ComboBoxItem) {isc.ComboBoxItem.addProperties({
        textBoxStyle:"selectItemText",
        showFocusedPickerIcon:true,
        pickerIconSrc:"[SKIN]/controls/comboBoxPicker.png",
        height:17,
        pickerIconWidth:18
    })}

    // used by SelectItem and ComboBoxItem for picklist
    if (isc.ScrollingMenu) {isc.ScrollingMenu.addProperties({
        showShadow:true,
        shadowDepth:5
    })}
    if (isc.DateItem) {
        isc.DateItem.addProperties({
            pickerIconWidth:16,
            pickerIconHeight:16,
            pickerIconSrc:"[SKIN]/controls/date_control.gif"
        })
    }
    if (isc.SpinnerItem) {
        isc.SpinnerItem.addProperties({
            textBoxStyle:"selectItemText",
            height:20
        })
        isc.SpinnerItem.INCREASE_ICON = isc.addProperties(isc.SpinnerItem.INCREASE_ICON, {
            width:12,
            height:10,
            showRollOver:true,
            showFocused:true,
            imgOnly:true,
            src:"[SKIN]/controls/spinner_control_increase.gif"
        })
        isc.SpinnerItem.DECREASE_ICON = isc.addProperties(isc.SpinnerItem.DECREASE_ICON, {
            width:12,
            height:10,
            showRollOver:true,
            showFocused:true,
            imgOnly:true,
            src:"[SKIN]/controls/spinner_control_decrease.gif"
        })
    }
    
    
    
    if (isc.PopUpTextAreaItem) {isc.PopUpTextAreaItem.addProperties({
        popUpIconSrc: "[SKIN]/controls/text_control.gif",
        popUpIconWidth:16,
        popUpIconHeight:16
    })}
    if (isc.ButtonItem && isc.IButton) {isc.ButtonItem.addProperties({
        showFocused:true,
        showFocusAsOver:false,
        buttonConstructor:isc.IButton,
        height:22
    })}

    if (isc.ToolbarItem && isc.IAutoFitButton) {isc.ToolbarItem.addProperties({
        buttonConstructor:isc.IAutoFitButton,
        buttonProperties: {
            autoFitDirection: isc.Canvas.BOTH
        }
    })}



//----------------------------------------
// 15) Drag & Drop
//----------------------------------------
    // drag tracker drop shadow (disabled by default because many trackers are irregular shape)
    //isc.addProperties(isc.EH.dragTrackerDefaults, {
    //    showShadow:true,
    //    shadowDepth:4
    //});
    // drag target shadow and opacity
    isc.EH.showTargetDragShadow = true;
    isc.EH.targetDragOpacity = 50;


    
//----------------------------------------
// 16) Edges
//----------------------------------------
    // default edge style serves as a pretty component frame/border - just set showEdges:true
    if (isc.EdgedCanvas) {
        isc.EdgedCanvas.addProperties({
            edgeSize:6,
            edgeImage:"[SKIN]/rounded/frame/FFFFFF/6.png"
        })
    }


//----------------------------------------
// 17) Sliders
//----------------------------------------
    if (isc.Slider) {
        isc.Slider.addProperties({
            thumbThickWidth:14,
            thumbThinWidth:14,
            trackWidth:5,
            trackCapSize:2,
            thumbSrc:"thumb.png",
            trackSrc:"track.png"
        })
    }

//----------------------------------------
// 18) TileList
//----------------------------------------
    if (isc.TileGrid) {
        isc.TileGrid.addProperties({
            valuesShowRollOver: true
        })
    }
    
// ----------------------------------------
// 19) CubeGrid
//----------------------------------------
    if (isc.CubeGrid) {
        isc.CubeGrid.addProperties({
            bodyStyleName:"cubeGridBody",
            alternateBodyStyleName:"alternateCubeGridBody"
        });
    }

// ----------------------------------------
// 20) FilterBuilder
//----------------------------------------
	if (isc.FilterBuilder) {
		isc.FilterBuilder.changeDefaults("addButtonDefaults", {
			showFocused: false
		});
		isc.FilterBuilder.changeDefaults("removeButtonDefaults", {
			showFocused: false
		});
	}

// -------------------------------------------
// 21) Printing
// -------------------------------------------
    if (isc.PrintWindow) {
        isc.PrintWindow.changeDefaults("printButtonDefaults", {
            height: 18
        });
    }
// -------------------------------------------
// ExampleViewPane - used in the feature explorer
// -------------------------------------------
    if (isc.ExampleViewPane) {
        isc.ExampleViewPane.addProperties({
            styleName:"normal"
        });
    }    

// specify where the browser should redirect if not supported
isc.Page.checkBrowserAndRedirect("[SKIN]/unsupported_browser.html");

}   // end with()
}   // end loadSkin()

isc.loadSkin();

