# ExampleAutocomplete.py
# -*- coding: latin-1 -*-
import os
import json
import urllib
import urlparse
import requests
from concurrent.futures import ProcessPoolExecutor
from requests import Session
from requests_futures.sessions import FuturesSession

import wx

URL_GEOCODER = "https://direcciones.ide.uy"

#-----------------------------------------------------------------------------
# Nota: Probado con python 2.7. Instalar wxPython, requests, urllib, requests-futures,json (pip install)
# Al escribir, se llama al método OnEnter, que si no es una pulsación de Intro
# llama a la función get_candidates. Dentro de esta función se utiliza la 
# librería rquests_futures para lanzar el request, de forma que sea asíncrono
# y la sensación de teclear sea fluida.
# Cuando un request devuelve algo, se llama al hook response_hook, donde
# revisamos que pertenece a lo último que se ha tecleado, y rellenamos
# el listbox con los candidatos encontrados.
# Cuando el usuario hace click, se utilizan los datos para hacer una llamada
# a la función find_direc, que usa la función request (síncrona) para
# obtener los datos de la dirección seleccionada. Si el usuario pulsa enter
# asumimos que quiere seleccionar la primera dirección en el listbox, y 
# usamos el primer item.
# Los dataos encontrados por find_direc se muestran en un label con el json 
# formateado para leerlo mejor.
#-----------------------------------------------------------------------------

class MyMainFrame(wx.Frame):
    def __init__(self):
        wx.Frame.__init__(self, None, title=wx.GetApp().GetAppName())
        self._session = FuturesSession(max_workers=4)
        self._lastSearch = ''

        self._createControls()

        self._connectControls()

    def response_hook(self, resp, *args, **kwargs):
        # parse the json storing the result on the response object
        # print resp.url
        url = urllib.unquote(resp.url.encode('utf-8'))
        # print url
        parsed = urlparse.urlparse(url)
        q = urlparse.parse_qs(parsed.query)['q'][0]
        # print q
        if self._lastSearch != q:
            # print 'salgo'
            # print self._lastSearch
            # print q
            return
        resp.data = resp.json()
        # print 'recibido ' + resp.url

        sArray = []
        self._candidates = resp.data
        for d in resp.data:
            sArray.append(d['address'])
        self._lstCandidatos.InsertItems(sArray, 0)


    def get_candidates(self, s):
        if len(s) == 0:
            return
        service_url = URL_GEOCODER + "/api/v1/geocode/candidates"
        params = {
            'q': s.encode('utf-8'),
            'limit':10,
            'soloLocalidad': False
        }
        service_url = service_url + '?' + urllib.urlencode(params)
        
        future = self._session.get(service_url, hooks={ 'response': self.response_hook })
        self._lastSearch = s.encode('utf-8')
        self._candidates = []

    def find_direc(self, address):
        # print(str(address))
        self.search.SetValue(address['address'])
        typeSearch = address['type']
        inmueble = address['inmueble'] 
        if (inmueble != None):
            typeSearch = "inmueble"
        
        params = {}
        params = {
            'type': typeSearch,
            'id': address['id'],
            'idcalle': address['idCalle'],
            'idcalleEsq': address['idCalleEsq'],
            'nomvia': address['nomVia'],
            'source': 'ide_uy',
            'localidad': address['localidad'],
            'departamento': address['departamento'],
            'inmueble': inmueble
        }
        if ('0' != address['portalNumber']):
            params['portal'] = address['portalNumber']
        if ('' != address['letra']):
            params['letra'] = address['letra']
            

        if ('' != address['manzana']):
            params['manzana'] = address['manzana']
            params['solar'] = address['solar']

        if ('0' != address['km']):
            params['km'] = address['km']

        # Podemos devolver varios resultados (para el caso en que hay portales repetidos, por ejemplo)
        service_url = URL_GEOCODER + "/api/v1/geocode/find"
        # service_url = service_url + '?' + urllib.urlencode(params)

        # print service_url
        r = requests.get(service_url, params = params)

        data = r.json()
        print 'recibo' + json.dumps(data, indent=2)
        self._labelResult.SetLabel(json.dumps(data, indent=2))


    def _createControls(self):
        # A Statusbar in the bottom of the window
        self.CreateStatusBar(1)
        sMsg = 'wxPython ' + wx.version()
        self.SetStatusText(sMsg)

        # Add a panel to the frame (needed under Windows to have a nice background)
        pnl = wx.Panel(self, wx.ID_ANY)

        szrMain = wx.BoxSizer(wx.VERTICAL)
        szrMain.AddSpacer(5)

        # stbSzr = wx.StaticBoxSizer(wx.VERTICAL, pnl, 'Demo de Geocoder :')
        # stBox = stbSzr.GetStaticBox()
        label = wx.StaticText(pnl, wx.ID_STATIC, 'Buscar dirección:')
        szrMain.Add(label, 0, wx.LEFT|wx.RIGHT|wx.TOP, 5)
        szrMain.AddSpacer(2)
        label = wx.StaticText(pnl, wx.ID_STATIC, 'El autocompletado utiliza el servicio de geocodificación.')
        szrMain.Add(label, 0, wx.LEFT|wx.RIGHT|wx.BOTTOM, 5)
        self.search = wx.TextCtrl(pnl, -1, wx.EmptyString)
        # Creando el buscador de calles 

        szrMain.Add(self.search, 0, wx.LEFT|wx.RIGHT|wx.BOTTOM|wx.EXPAND, 5)
        self._lstCandidatos = wx.ListBox(pnl)
        self._lstCandidatos.InsertItems(['uno', 'dos'], 0)
        szrMain.Add(self._lstCandidatos, 0, wx.LEFT|wx.RIGHT|wx.BOTTOM|wx.EXPAND, 5)
        self._labelResult = wx.StaticText(pnl, wx.ID_STATIC, '')
        self._labelResult.Wrap(300)
        self._labelResult.Bind(wx.EVT_SIZE, self.__WrapText__)
        szrMain.Add(self._labelResult, 0, wx.LEFT|wx.RIGHT|wx.BOTTOM|wx.EXPAND|wx.ALL, 5)

        # prueba = wx.TextCtrl()
        # szrMain.Add(prueba, 0, wx.LEFT|wx.RIGHT|wx.BOTTOM|wx.EXPAND, 5)

        # szrMain.Add(stbSzr, 0, wx.LEFT|wx.RIGHT|wx.BOTTOM|wx.EXPAND, 5)

        pnl.SetSizer(szrMain)
        szrMain.SetSizeHints(self)

        # self._txtEntry1.AutoComplete(MyClassCompleterSimple())
        # self.search.AutoComplete([])
    
    def __WrapText__(self, event):
        # self._labelResult.Wrap(event.GetSize()[0])
        # print event.GetSize()[0]
        self._labelResult.Wrap(400)
        event.Skip()

    def _connectControls(self):
        print 'Dentro de connectControls'
        self.search.Bind(wx.EVT_KEY_UP, self.OnEnter)
        self.Bind(wx.EVT_LISTBOX, self.OnListBox) 
 


    def OnListBox(self, event):
        print 'OnListBox'
        text = event.GetEventObject().GetStringSelection()
        print text.encode('utf-8')
        self.search.SetValue(text)
        selectedIndex = event.GetEventObject().GetSelection()
        address = self._candidates[selectedIndex]
        self.find_direc(address)

    def OnEnter(self, event):
        """Send a search event"""
        print 'OnEnter'
        code = event.GetKeyCode()
        val = self.search.GetValue()
        if code == wx.WXK_RETURN and val:
            # Usamos el primer candidato para buscar los todos sus datos
            self.find_direc(self._candidates[0])
        else:
            # Buscar candidatos
            self._lstCandidatos.Clear()
            aux = self.get_candidates(val)

#---------------------------------------------------------------------------

class MyApp(wx.App):
    def OnInit(self):
        print('Running wxPython ' + wx.version())
        # Set Current directory to the one containing this file
        os.chdir(os.path.dirname(os.path.abspath(__file__)))

        self.SetAppName('AutoComplete')

        # Create the main window
        frm = MyMainFrame()
        frm.SetInitialSize(wx.Size(700, 500))
        self.SetTopWindow(frm)

        frm.Show()
        return True

#---------------------------------------------------------------------------

if __name__ == '__main__':
    app = MyApp()
    app.MainLoop()