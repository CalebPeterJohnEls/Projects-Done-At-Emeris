import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Dimensions, Image } from 'react-native';
import { Table, Row, Rows } from 'react-native-table-component';
import { Header } from 'react-native-elements';

const { width, height } = Dimensions.get('window');

const History = ({ route, navigation }) => {
  const { books } = route.params || { books: [] };

  const tableData = books.map((book, index) => [index + 1, book.title, book.author]);


  for (let i = books.length; i < 3; i++) {
    tableData.push([i + 1, '', '']);
  }

  const tableHeader = ['', 'Title', 'Author'];
//Fill data to table

  return (
    <View style={styles.container}>
      <View style={[styles.background, styles.overflow]}></View>
      <Header
        containerStyle={{ marginTop: -20 }}
        backgroundColor="#21c46b"
        leftComponent={
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            <Image source={require('./assets/LOGO.png')} style={styles.Logo} />

            <Text style={styles.title}>H</Text>
            <Text style={styles.title}>I</Text>
            <Text style={styles.title}>S</Text>
            <Text style={styles.title}>T</Text>
            <Text style={styles.title}>O</Text>
            <Text style={styles.title}>R</Text>
            <Text style={styles.title}>Y</Text>
          </View>//To fix issue of letters stacking
        }
      />
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text style={styles.title2}>LAST 3 BOOKS READ</Text>
      <View style={styles.tableContainer}>
        <Table borderStyle={{ borderWidth: 5, borderColor: '#6aec8e' }}>
          <Row
            data={tableHeader}
            style={styles.tableHeader}
            textStyle={styles.tableHeaderText}
          />
          <Rows data={tableData} textStyle={styles.tableRowText} />
        </Table>
      </View>
      <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 20 }}>
        <TouchableOpacity onPress={() => navigation.navigate('Home')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>HOME</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => navigation.navigate('AddBook')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>ADD BOOK</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => navigation.navigate('Genres')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>GENRES</Text>
          </View>
        </TouchableOpacity>
      </View>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1,
    backgroundColor: '#217bc4',
  },
  title: {
    fontSize: 36,
    color: '#1E78CB',
    marginBottom: 24,
    textAlign: 'center',
  },
  title2: {
    fontSize: 30,
    color: '#6aec8e',
    marginBottom: 24,
    textAlign: 'center',
  },
  tableContainer: {
    flex: 1,
    width: width * 0.9,
    marginVertical: 20,
  },
  tableHeader: {
    height: 40,
    backgroundColor: '#1E78CB',
  },
  tableHeaderText: {
    fontSize: 25,
    color: '#6aec8e',
    textAlign: 'center',
  },
  tableRowText: {
    fontSize: 25,
    color: '#6aec8e',
    textAlign: 'center',
  },
  btnText: {
    fontSize: 20,
    color: '#217bc4',
    fontWeight: '800',
  },
  button: {
    borderColor: '#1E78CB',
    borderWidth: 2,
    padding: 10,
    backgroundColor: '#21c46b',
    alignItems: 'center',
    borderRadius: 30,
    height: 60,
    width: 150,
    marginHorizontal: 10,
  },
  Logo: {
    width: 200,
    height: 210,
    marginRight: 20,
  },
});

export default History;



//IIE, 2023. 


//References: 

//IIE, 2023. MOBILE APP SCRIPTING MAST5112 [Module Manual]. The Independent Institute of Education: Unpublished 

//Figma.2023. [Online] Available at: https://www.figma.com/. Accessed [07/09/2023]  

//Canva 2023.[Online] Available at: https://www.canva.com/. Accessed [07/08/2023] 

//Fandom.2023.[Online] Available at: https://adventuretimesuperfans.fandom.com/wiki/Shelby . Accessed [07/08/2023] 

//Stack Overflow. 2023. [Online] Available at:   https://stackoverflow.com/questions/55382042/how-to-show-amount-of-times-user-has-logged-in-using-asyncstorage. Accessed [24/11/2023]

//Bobbyhadz.com. 2023. How to use the forEach() method in React, 2023 [Online] Available at:   https://bobbyhadz.com/blog/react-foreach. Accessed [24/11/2023]

//npmjs. 2021. react-native-table-component, 2021 [Online] Available at:   https://www.npmjs.com/package/react-native-table-component. Accessed [22/11/2023]

//Geeks for Geeks. 2023. How to Create A Simple Counter App using React Native ?, 2023 [Online] Available at:   https://www.geeksforgeeks.org/how-to-create-a-simple-counter-app-using-react-native/. Accessed [24/11/2023]